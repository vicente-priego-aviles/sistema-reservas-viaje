package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarCocheDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaCocheRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.ReservarCocheCasoUso;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Worker de Camunda para procesar la tarea de reserva de coche de alquiler.
 * Escucha el task type "reservar-coche" del proceso BPMN y ejecuta la lógica
 * de negocio delegando en el servicio de aplicación correspondiente.
 *
 * <p>Este worker forma parte del subproceso de Pagos y se ejecuta en paralelo
 * con las Pagos de vuelo y hotel.</p>
 *
 * @see ReservarCocheCasoUso
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaCocheWorker {

    private final ReservarCocheCasoUso reservarCocheCasoUso;

    /**
     * Procesa la tarea de reservar un coche de alquiler.
     *
     * <p>Variables de entrada esperadas del proceso:
     * <ul>
     *   <li>clienteId (String): ID del cliente</li>
     *   <li>empresaAlquiler (String): Nombre de la empresa de alquiler</li>
     *   <li>modeloCoche (String): Modelo del coche</li>
     *   <li>categoriaCoche (String): Categoría del coche (ECONOMICO, COMPACTO, SUV, etc.)</li>
     *   <li>ubicacionRecogida (String): Ubicación donde se recoge el coche</li>
     *   <li>ubicacionDevolucion (String): Ubicación donde se devuelve el coche</li>
     *   <li>fechaRecogida (String): Fecha y hora de recogida en formato ISO</li>
     *   <li>fechaDevolucion (String): Fecha y hora de devolución en formato ISO</li>
     *   <li>precioCoche (Double): Precio total del alquiler</li>
     *   <li>codigoMoneda (String, opcional): Código de moneda (por defecto EUR)</li>
     *   <li>observacionesCoche (String, opcional): Observaciones adicionales</li>
     * </ul>
     * </p>
     *
     * <p>Variables de salida devueltas al proceso:
     * <ul>
     *   <li>reservaCocheId (String): ID de la reserva creada</li>
     *   <li>reservaCocheExitosa (Boolean): true si la reserva fue exitosa</li>
     *   <li>codigoConfirmacionCoche (String): Código de confirmación de la reserva</li>
     *   <li>precioCocheFinal (Double): Precio final de la reserva</li>
     *   <li>diasAlquiler (Integer): Número de días de alquiler</li>
     * </ul>
     * </p>
     *
     * @param job Job activado de Zeebe con las variables del proceso
     * @return Mapa con las variables de salida para el proceso
     * @throws ZeebeBpmnError Si ocurre un error durante la reserva
     */
    @JobWorker(type = "reservar-coche", autoComplete = true)
    public Map<String, Object> reservarCoche(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        log.info("🚀 Iniciando worker de reserva de coche - Job Key: {}", job.getKey());
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Validar variables requeridas
            validarVariablesRequeridas(variables);

            // 2. Mapear variables de Camunda a DTO
            ReservarCocheDTO dto = mapearADTO(variables);

            log.info("🚗 Procesando reserva de coche {} de {} para cliente: {}",
                    dto.modeloCoche(), dto.empresaAlquiler(), dto.clienteId());

            // 3. Ejecutar caso de uso
            ReservaCocheRespuestaDTO respuesta = reservarCocheCasoUso.ejecutar(dto);

            log.info("✅ Reserva de coche completada exitosamente - ID: {}",
                    respuesta.reservaId());

            // 4. Preparar variables de salida
            Map<String, Object> resultado = Map.of(
                    "reservaCocheId", respuesta.reservaId(),
                    "reservaCocheExitosa", true,
                    "codigoConfirmacionCoche", respuesta.codigoConfirmacion() != null
                            ? respuesta.codigoConfirmacion()
                            : "",
                    "precioCocheFinal", respuesta.precio().doubleValue(),
                    "diasAlquiler", respuesta.diasAlquiler(),
                    "estadoReservaCoche", respuesta.estado()
            );

            log.info("📤 Variables de salida preparadas: {}", resultado);

            return resultado;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación en reserva de coche: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_VALIDACION_COCHE",
                    "Error de validación en los datos del coche: " + e.getMessage(),
                    Map.of()
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar reserva de coche: {}",
                    e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_RESERVA_COCHE",
                    "Error al procesar la reserva de coche: " + e.getMessage(),
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
                "clienteId", "empresaAlquiler", "modeloCoche", "categoriaCoche",
                "ubicacionRecogida", "ubicacionDevolucion",
                "fechaRecogida", "fechaDevolucion", "precioCoche"
        };

        for (String campo : camposRequeridos) {
            if (!variables.containsKey(campo) || variables.get(campo) == null) {
                throw new IllegalArgumentException(
                        "El campo requerido '" + campo + "' no está presente o es nulo"
                );
            }
        }

        log.debug("✅ Validación de variables requeridas completada");
    }

    /**
     * Mapea las variables del proceso de Camunda a un DTO de entrada.
     *
     * @param variables Variables del proceso
     * @return DTO con los datos para crear la reserva
     */
    private ReservarCocheDTO mapearADTO(Map<String, Object> variables) {
        try {
            // Mapear fechas
            LocalDateTime fechaRecogida = parseDateTime(variables.get("fechaRecogida"));
            LocalDateTime fechaDevolucion = parseDateTime(variables.get("fechaDevolucion"));

            // Validar que la fecha de devolución sea posterior a la de recogida
            if (!fechaDevolucion.isAfter(fechaRecogida)) {
                throw new IllegalArgumentException(
                        "La fecha de devolución debe ser posterior a la fecha de recogida"
                );
            }

            // Crear DTO
            return new ReservarCocheDTO(
                    (String) variables.get("clienteId"),
                    (String) variables.get("empresaAlquiler"),
                    (String) variables.get("modeloCoche"),
                    (String) variables.get("categoriaCoche"),
                    (String) variables.get("ubicacionRecogida"),
                    (String) variables.get("ubicacionDevolucion"),
                    fechaRecogida,
                    fechaDevolucion,
                    convertirABigDecimal(variables.get("precioCoche")),
                    (String) variables.getOrDefault("codigoMoneda", "EUR"),
                    (String) variables.get("observacionesCoche"),
                    (String) variables.get("codigoConfirmacion")
            );

        } catch (IllegalArgumentException e) {
            throw e; // Re-lanzar excepciones de validación
        } catch (Exception e) {
            log.error("❌ Error al mapear variables a DTO: {}", e.getMessage());
            throw new IllegalArgumentException(
                    "Error al procesar los datos del coche: " + e.getMessage(), e
            );
        }
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
     * @param fechaObj Objeto que contiene la fecha (String ISO o LocalDateTime)
     * @return LocalDateTime parseado
     */
    private LocalDateTime parseDateTime(Object fechaObj) {
        if (fechaObj == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        if (fechaObj instanceof LocalDateTime) {
            return (LocalDateTime) fechaObj;
        } else if (fechaObj instanceof String) {
            return LocalDateTime.parse((String) fechaObj);
        }

        throw new IllegalArgumentException(
                "Formato de fecha no soportado: " + fechaObj.getClass()
        );
    }
}

