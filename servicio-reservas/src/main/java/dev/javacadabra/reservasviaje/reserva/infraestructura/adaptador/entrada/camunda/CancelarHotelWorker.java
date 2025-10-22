package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.CancelarHotelCasoUso;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Worker de Camunda para procesar la compensación de reserva de hotel.
 * Escucha el task type "cancelar-hotel" del proceso BPMN (tarea de compensación)
 * y ejecuta la lógica de cancelación cuando se necesita revertir una reserva.
 *
 * <p>Este worker se ejecuta como parte del patrón Saga cuando ocurre un error
 * en el proceso de reserva y es necesario deshacer las operaciones ya realizadas.</p>
 *
 * <p><strong>Nota:</strong> Esta tarea está marcada como isForCompensation=true en el BPMN,
 * por lo que solo se ejecuta cuando se dispara un evento de compensación.</p>
 *
 * @see CancelarHotelCasoUso
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CancelarHotelWorker {

    private final CancelarHotelCasoUso cancelarHotelCasoUso;

    /**
     * Procesa la tarea de cancelar una reserva de hotel (compensación).
     *
     * <p>Variables de entrada esperadas del proceso:
     * <ul>
     *   <li>reservaHotelId (String): ID de la reserva de hotel a cancelar</li>
     *   <li>motivoCancelacion (String, opcional): Motivo de la cancelación</li>
     * </ul>
     * </p>
     *
     * <p>Variables de salida devueltas al proceso:
     * <ul>
     *   <li>hotelCancelado (Boolean): true si la cancelación fue exitosa</li>
     *   <li>mensajeCancelacionHotel (String): Mensaje con el resultado de la cancelación</li>
     * </ul>
     * </p>
     *
     * <p><strong>Comportamiento ante errores:</strong> Si la cancelación falla,
     * se registra el error pero NO se lanza una excepción para evitar bloquear
     * el flujo de compensación. Se retorna hotelCancelado=false.</p>
     *
     * @param job Job activado de Zeebe con las variables del proceso
     * @return Mapa con las variables de salida para el proceso
     */
    @JobWorker(type = "cancelar-hotel", autoComplete = true)
    public Map<String, Object> cancelarHotel(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        log.info("🛑 Iniciando worker de cancelación de hotel (compensación) - Job Key: {}",
                job.getKey());
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Obtener ID de reserva
            String reservaHotelId = obtenerReservaHotelId(variables);

            if (reservaHotelId == null || reservaHotelId.isBlank()) {
                log.warn("⚠️ No se encontró ID de reserva de hotel para cancelar. " +
                         "Probablemente la reserva no llegó a crearse.");

                return Map.of(
                        "hotelCancelado", true,
                        "mensajeCancelacionHotel", "No había reserva de hotel que cancelar"
                );
            }

            // 2. Obtener motivo de cancelación
            String motivo = (String) variables.getOrDefault("motivoCancelacion",
                    "Compensación por error en el proceso de reserva");

            log.info("❌ Cancelando reserva de hotel: {} - Motivo: {}",
                    reservaHotelId, motivo);

            // 3. Ejecutar caso de uso de cancelación
            cancelarHotelCasoUso.ejecutar(reservaHotelId, motivo);

            log.info("✅ Reserva de hotel cancelada exitosamente: {}", reservaHotelId);

            // 4. Preparar variables de salida
            return Map.of(
                    "hotelCancelado", true,
                    "mensajeCancelacionHotel",
                    "Reserva de hotel " + reservaHotelId + " cancelada correctamente"
            );

        } catch (Exception e) {
            log.error("❌ Error al cancelar reserva de hotel: {}", e.getMessage(), e);

            // En compensaciones, NO lanzamos error para no bloquear el flujo
            // Solo registramos el fallo y retornamos false
            return Map.of(
                    "hotelCancelado", false,
                    "mensajeCancelacionHotel",
                    "Error al cancelar hotel: " + e.getMessage()
            );
        }
    }

    /**
     * Obtiene el ID de la reserva de hotel desde las variables del proceso.
     * Intenta múltiples claves posibles para mayor flexibilidad.
     *
     * @param variables Variables del proceso
     * @return ID de la reserva o null si no se encuentra
     */
    private String obtenerReservaHotelId(Map<String, Object> variables) {
        // Intentar diferentes claves posibles
        Object id = variables.get("reservaHotelId");
        if (id == null) {
            id = variables.get("hotelReservaId");
        }
        if (id == null) {
            id = variables.get("idReservaHotel");
        }

        return id != null ? id.toString() : null;
    }
}

