package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarVueloDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaVueloRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.ReservarVueloCasoUso;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Worker de Camunda para procesar la tarea de reserva de vuelo.
 * Escucha el task type "reservar-vuelo" del proceso BPMN y ejecuta la lógica
 * de negocio delegando en el servicio de aplicación correspondiente.
 *
 * <p>Este worker forma parte del subproceso de Pagos y se ejecuta en paralelo
 * con las Pagos de hotel y coche.</p>
 *
 * @see ReservarVueloCasoUso
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaVueloWorker {

    private final ReservarVueloCasoUso reservarVueloCasoUso;

    /**
     * Procesa la tarea de reservar un vuelo.
     *
     * <p>Variables de entrada esperadas del proceso:
     * <ul>
     *   <li>clienteId (String): ID del cliente</li>
     *   <li>numeroVuelo (String): Número del vuelo</li>
     *   <li>aerolinea (String): Nombre de la aerolínea</li>
     *   <li>origen (String): Ciudad de origen</li>
     *   <li>destino (String): Ciudad de destino</li>
     *   <li>fechaSalida (String): Fecha y hora de salida en formato ISO</li>
     *   <li>fechaLlegada (String): Fecha y hora de llegada en formato ISO</li>
     *   <li>clase (String): Clase del vuelo (ECONOMICA, BUSINESS, PRIMERA)</li>
     *   <li>precioVuelo (Double): Precio del vuelo</li>
     *   <li>codigoMoneda (String, opcional): Código de moneda (por defecto EUR)</li>
     *   <li>pasajeros (List): Lista de pasajeros con sus datos</li>
     *   <li>observacionesVuelo (String, opcional): Observaciones adicionales</li>
     * </ul>
     * </p>
     *
     * <p>Variables de salida devueltas al proceso:
     * <ul>
     *   <li>reservaVueloId (String): ID de la reserva creada</li>
     *   <li>reservaVueloExitosa (Boolean): true si la reserva fue exitosa</li>
     *   <li>codigoConfirmacionVuelo (String): Código de confirmación de la reserva del vuelo</li>
     *   <li>precioVueloFinal (Double): Precio final de la reserva</li>
     * </ul>
     * </p>
     *
     * @param job Job activado de Zeebe con las variables del proceso
     * @return Mapa con las variables de salida para el proceso
     * @throws ZeebeBpmnError Si ocurre un error durante la reserva
     */
    @JobWorker(type = "reservar-vuelo", autoComplete = true)
    public Map<String, Object> reservarVuelo(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        log.info("🚀 Iniciando worker de reserva de vuelo - Job Key: {}", job.getKey());
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Validar variables requeridas
            validarVariablesRequeridas(variables);

            // 2. Mapear variables de Camunda a DTO
            ReservarVueloDTO dto = mapearADTO(variables);

            log.info("✈️ Procesando reserva de vuelo {} para cliente: {}",
                    dto.numeroVuelo(), dto.clienteId());

            // 3. Ejecutar caso de uso
            ReservaVueloRespuestaDTO respuesta = reservarVueloCasoUso.ejecutar(dto);

            log.info("✅ Reserva de vuelo completada exitosamente - ID: {}",
                    respuesta.reservaId());

            // 4. Preparar variables de salida
            Map<String, Object> resultado = Map.of(
                    "reservaVueloId", respuesta.reservaId(),
                    "reservaVueloExitosa", true,
                    "codigoConfirmacionVuelo", respuesta.codigoConfirmacion() != null
                            ? respuesta.codigoConfirmacion()
                            : "",
                    "precioVueloFinal", respuesta.precio().doubleValue(),
                    "estadoReservaVuelo", respuesta.estado()
            );

            log.info("📤 Variables de salida preparadas: {}", resultado);

            return resultado;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación en reserva de vuelo: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_VALIDACION_VUELO",
                    "Error de validación en los datos del vuelo: " + e.getMessage(),
                    Map.of()
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar reserva de vuelo: {}",
                    e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_RESERVA_VUELO",
                    "Error al procesar la reserva de vuelo: " + e.getMessage(),
                    Map.of()
            );
        }
    }

    /**
     * Valida que todas las variables requeridas estén presentes en el job.
     *
     * @param variables Variables del proceso
     * @throws IllegalArgumentException Si falta alguna variable requerida
     */
    private void validarVariablesRequeridas(Map<String, Object> variables) {
        String[] camposRequeridos = {
                "clienteId", "numeroVuelo", "aerolinea", "origen", "destino",
                "fechaSalida", "fechaLlegada", "clase", "precioVuelo", "pasajeros"
        };

        for (String campo : camposRequeridos) {
            if (!variables.containsKey(campo) || variables.get(campo) == null) {
                throw new IllegalArgumentException(
                        "El campo requerido '" + campo + "' no está presente o es nulo"
                );
            }
        }

        // Validar que la lista de pasajeros no esté vacía
        Object pasajerosObj = variables.get("pasajeros");
        if (pasajerosObj instanceof List) {
            List<?> pasajeros = (List<?>) pasajerosObj;
            if (pasajeros.isEmpty()) {
                throw new IllegalArgumentException(
                        "La lista de pasajeros no puede estar vacía"
                );
            }
        } else {
            throw new IllegalArgumentException(
                    "El campo 'pasajeros' debe ser una lista"
            );
        }

        log.debug("✅ Validación de variables requeridas completada");
    }

    /**
     * Mapea las variables del proceso de Camunda a un DTO de entrada.
     *
     * @param variables Variables del proceso
     * @return DTO con los datos para crear la reserva
     */
    private ReservarVueloDTO mapearADTO(Map<String, Object> variables) {
        try {
            // Mapear pasajeros
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pasajerosMap = (List<Map<String, Object>>)
                    variables.get("pasajeros");

            List<ReservarVueloDTO.PasajeroDTO> pasajeros = pasajerosMap.stream()
                    .map(this::mapearPasajero)
                    .collect(Collectors.toList());

            // Mapear fechas
            LocalDateTime fechaSalida = parseDateTime(variables.get("fechaSalida"));
            LocalDateTime fechaLlegada = parseDateTime(variables.get("fechaLlegada"));

            // Crear DTO
            return new ReservarVueloDTO(
                    (String) variables.get("clienteId"),
                    (String) variables.get("numeroVuelo"),
                    (String) variables.get("aerolinea"),
                    (String) variables.get("origen"),
                    (String) variables.get("destino"),
                    fechaSalida,
                    fechaLlegada,
                    (String) variables.get("clase"),
                    convertirABigDecimal(variables.get("precioVuelo")),
                    (String) variables.getOrDefault("codigoMoneda", "EUR"),
                    pasajeros,
                    (String) variables.get("observacionesVuelo"),
                    (String) variables.get("codigoConfirmacionVuelo")
            );

        } catch (Exception e) {
            log.error("❌ Error al mapear variables a DTO: {}", e.getMessage());
            throw new IllegalArgumentException(
                    "Error al procesar los datos del vuelo: " + e.getMessage(), e
            );
        }
    }

    /**
     * Mapea los datos de un pasajero desde el mapa de variables.
     *
     * @param pasajeroMap Mapa con los datos del pasajero
     * @return DTO del pasajero
     */
    private ReservarVueloDTO.PasajeroDTO mapearPasajero(Map<String, Object> pasajeroMap) {
        LocalDate fechaNacimiento = parseDate(pasajeroMap.get("fechaNacimiento"));

        return new ReservarVueloDTO.PasajeroDTO(
                (String) pasajeroMap.get("nombre"),
                (String) pasajeroMap.get("apellidos"),
                (String) pasajeroMap.get("numeroDocumento"),
                (String) pasajeroMap.get("tipoDocumento"),
                fechaNacimiento,
                (String) pasajeroMap.get("nacionalidad")
        );
    }

    /**
     * Convierte un objeto a BigDecimal de forma segura.
     *
     * @param valor Valor a convertir (puede ser Double, Integer, String, etc.)
     * @return BigDecimal equivalente
     */
    private BigDecimal convertirABigDecimal(Object valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El precio no puede ser nulo");
        }

        if (valor instanceof BigDecimal) {
            return (BigDecimal) valor;
        } else if (valor instanceof Number) {
            return BigDecimal.valueOf(((Number) valor).doubleValue());
        } else if (valor instanceof String) {
            return new BigDecimal((String) valor);
        }

        throw new IllegalArgumentException(
                "No se puede convertir el precio a BigDecimal: " + valor.getClass()
        );
    }

    /**
     * Parsea una fecha-hora desde diferentes formatos posibles.
     *
     * CORREGIDO: Ahora soporta el formato con offset de Camunda.
     * Formatos soportados:
     * - "2025-05-20T12:00+02:00" (formato de Camunda con offset)
     * - "2025-05-20T12:00:00" (formato ISO sin offset)
     * - LocalDateTime directo
     *
     * @param fechaObj Objeto que contiene la fecha (String ISO o LocalDateTime)
     * @return LocalDateTime parseado
     * @throws IllegalArgumentException Si el formato no es válido
     */
    private LocalDateTime parseDateTime(Object fechaObj) {
        if (fechaObj == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        if (fechaObj instanceof LocalDateTime) {
            return (LocalDateTime) fechaObj;
        }

        if (fechaObj instanceof String) {
            String fechaStr = (String) fechaObj;

            try {
                // Intentar parsear con offset (formato de Camunda)
                // "2025-05-20T12:00+02:00" → OffsetDateTime → LocalDateTime
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(fechaStr);
                return offsetDateTime.toLocalDateTime();

            } catch (DateTimeParseException e) {
                // Si falla, intentar parsear como LocalDateTime estándar
                // "2025-05-20T12:00:00" (sin offset)
                try {
                    return LocalDateTime.parse(fechaStr);
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException(
                            "Formato de fecha no válido. Se esperaba ISO 8601 con o sin offset. " +
                            "Recibido: " + fechaStr, e2
                    );
                }
            }
        }

        throw new IllegalArgumentException(
                "Formato de fecha no soportado: " + fechaObj.getClass()
        );
    }

    /**
     * Parsea una fecha desde diferentes formatos posibles.
     *
     * @param fechaObj Objeto que contiene la fecha (String ISO o LocalDate)
     * @return LocalDate parseado
     */
    private LocalDate parseDate(Object fechaObj) {
        if (fechaObj == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        if (fechaObj instanceof LocalDate) {
            return (LocalDate) fechaObj;
        } else if (fechaObj instanceof String) {
            return LocalDate.parse((String) fechaObj);
        }

        throw new IllegalArgumentException(
                "Formato de fecha no soportado: " + fechaObj.getClass()
        );
    }
}
