package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarHotelDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaHotelRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.ReservarHotelCasoUso;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Worker de Camunda para procesar la tarea de reserva de hotel.
 * Escucha el task type "reservar-hotel" del proceso BPMN y ejecuta la lógica
 * de negocio delegando en el servicio de aplicación correspondiente.
 *
 * <p>Este worker forma parte del subproceso de Pagos y se ejecuta en paralelo
 * con las Pagos de vuelo y coche.</p>
 *
 * @see ReservarHotelCasoUso
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaHotelWorker {

    private final ReservarHotelCasoUso reservarHotelCasoUso;

    /**
     * Procesa la tarea de reservar un hotel.
     *
     * <p>Variables de entrada esperadas del proceso:
     * <ul>
     *   <li>clienteId (String): ID del cliente</li>
     *   <li>nombreHotel (String): Nombre del hotel</li>
     *   <li>ciudad (String): Ciudad donde se encuentra el hotel</li>
     *   <li>direccion (String): Dirección del hotel</li>
     *   <li>fechaEntrada (String): Fecha de entrada en formato ISO (yyyy-MM-dd)</li>
     *   <li>fechaSalida (String): Fecha de salida en formato ISO (yyyy-MM-dd)</li>
     *   <li>tipoHabitacion (String): Tipo de habitación (INDIVIDUAL, DOBLE, SUITE, etc.)</li>
     *   <li>numeroHabitaciones (Integer): Número de habitaciones</li>
     *   <li>numeroHuespedes (Integer): Número de huéspedes</li>
     *   <li>precioHotel (Double): Precio total del hotel</li>
     *   <li>codigoMoneda (String, opcional): Código de moneda (por defecto EUR)</li>
     *   <li>observacionesHotel (String, opcional): Observaciones adicionales</li>
     * </ul>
     * </p>
     *
     * <p>Variables de salida devueltas al proceso:
     * <ul>
     *   <li>reservaHotelId (String): ID de la reserva creada</li>
     *   <li>reservaHotelExitosa (Boolean): true si la reserva fue exitosa</li>
     *   <li>codigoConfirmacionHotel (String): Código de confirmación de la reserva del hotel</li>
     *   <li>precioHotelFinal (Double): Precio final de la reserva</li>
     *   <li>numeroNoches (Integer): Número de noches de la estancia</li>
     * </ul>
     * </p>
     *
     * @param job Job activado de Zeebe con las variables del proceso
     * @return Mapa con las variables de salida para el proceso
     * @throws ZeebeBpmnError Si ocurre un error durante la reserva
     */
    @JobWorker(type = "reservar-hotel", autoComplete = true)
    public Map<String, Object> reservarHotel(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        log.info("🚀 Iniciando worker de reserva de hotel - Job Key: {}", job.getKey());
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Validar variables requeridas
            validarVariablesRequeridas(variables);

            // 2. Mapear variables de Camunda a DTO
            ReservarHotelDTO dto = mapearADTO(variables);

            log.info("🏨 Procesando reserva de hotel {} en {} para cliente: {}",
                    dto.nombreHotel(), dto.ciudad(), dto.clienteId());

            // 3. Ejecutar caso de uso
            ReservaHotelRespuestaDTO respuesta = reservarHotelCasoUso.ejecutar(dto);

            log.info("✅ Reserva de hotel completada exitosamente - ID: {}",
                    respuesta.reservaId());

            // 4. Preparar variables de salida
            Map<String, Object> resultado = Map.of(
                    "reservaHotelId", respuesta.reservaId(),
                    "reservaHotelExitosa", true,
                    "codigoConfirmacionHotel", respuesta.codigoConfirmacion() != null
                            ? respuesta.codigoConfirmacion()
                            : "",
                    "precioHotelFinal", respuesta.precio().doubleValue(),
                    "numeroNoches", respuesta.numeroNoches(),
                    "estadoReservaHotel", respuesta.estado()
            );

            log.info("📤 Variables de salida preparadas: {}", resultado);

            return resultado;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación en reserva de hotel: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_VALIDACION_HOTEL",
                    "Error de validación en los datos del hotel: " + e.getMessage(),
                    Map.of()
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar reserva de hotel: {}",
                    e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_RESERVA_HOTEL",
                    "Error al procesar la reserva de hotel: " + e.getMessage(),
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
                "clienteId", "nombreHotel", "ciudad", "direccion",
                "fechaEntrada", "fechaSalida", "tipoHabitacion",
                "numeroHabitaciones", "numeroHuespedes", "precioHotel"
        };

        for (String campo : camposRequeridos) {
            if (!variables.containsKey(campo) || variables.get(campo) == null) {
                throw new IllegalArgumentException(
                        "El campo requerido '" + campo + "' no está presente o es nulo"
                );
            }
        }

        // Validaciones adicionales
        validarNumeroPositivo(variables.get("numeroHabitaciones"), "numeroHabitaciones");
        validarNumeroPositivo(variables.get("numeroHuespedes"), "numeroHuespedes");

        log.debug("✅ Validación de variables requeridas completada");
    }

    /**
     * Valida que un número sea positivo.
     *
     * @param valor Valor a validar
     * @param nombreCampo Nombre del campo para el mensaje de error
     * @throws IllegalArgumentException Si el valor no es positivo
     */
    private void validarNumeroPositivo(Object valor, String nombreCampo) {
        if (valor instanceof Number) {
            int numero = ((Number) valor).intValue();
            if (numero <= 0) {
                throw new IllegalArgumentException(
                        "El campo '" + nombreCampo + "' debe ser mayor a cero"
                );
            }
        }
    }

    /**
     * Mapea las variables del proceso de Camunda a un DTO de entrada.
     *
     * @param variables Variables del proceso
     * @return DTO con los datos para crear la reserva
     */
    private ReservarHotelDTO mapearADTO(Map<String, Object> variables) {
        try {
            // Mapear fechas
            LocalDate fechaEntrada = parseDate(variables.get("fechaEntrada"));
            LocalDate fechaSalida = parseDate(variables.get("fechaSalida"));

            // Validar que la fecha de salida sea posterior a la de entrada
            if (!fechaSalida.isAfter(fechaEntrada)) {
                throw new IllegalArgumentException(
                        "La fecha de salida debe ser posterior a la fecha de entrada"
                );
            }

            // Crear DTO
            return new ReservarHotelDTO(
                    (String) variables.get("clienteId"),
                    (String) variables.get("nombreHotel"),
                    (String) variables.get("ciudad"),
                    (String) variables.get("direccion"),
                    fechaEntrada,
                    fechaSalida,
                    (String) variables.get("tipoHabitacion"),
                    convertirAEntero(variables.get("numeroHabitaciones")),
                    convertirAEntero(variables.get("numeroHuespedes")),
                    convertirABigDecimal(variables.get("precioHotel")),
                    (String) variables.getOrDefault("codigoMoneda", "EUR"),
                    (String) variables.get("observacionesHotel"),
                    (String) variables.get("codigoConfirmacionHotel")
            );

        } catch (IllegalArgumentException e) {
            throw e; // Re-lanzar excepciones de validación
        } catch (Exception e) {
            log.error("❌ Error al mapear variables a DTO: {}", e.getMessage());
            throw new IllegalArgumentException(
                    "Error al procesar los datos del hotel: " + e.getMessage(), e
            );
        }
    }

    /**
     * Convierte un objeto a Integer de forma segura.
     *
     * @param valor Valor a convertir (puede ser Integer, Double, String, etc.)
     * @return Integer equivalente
     */
    private Integer convertirAEntero(Object valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor numérico no puede ser nulo");
        }

        if (valor instanceof Integer) {
            return (Integer) valor;
        } else if (valor instanceof Number) {
            return ((Number) valor).intValue();
        } else if (valor instanceof String) {
            return Integer.parseInt((String) valor);
        }

        throw new IllegalArgumentException(
                "No se puede convertir el valor a Integer: " + valor.getClass()
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
