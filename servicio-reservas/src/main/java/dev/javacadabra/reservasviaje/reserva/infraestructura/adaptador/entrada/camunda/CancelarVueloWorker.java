package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.CancelarVueloCasoUso;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Worker de Camunda para procesar la compensación de reserva de vuelo.
 * Escucha el task type "cancelar-vuelo" del proceso BPMN (tarea de compensación)
 * y ejecuta la lógica de cancelación cuando se necesita revertir una reserva.
 *
 * <p>Este worker se ejecuta como parte del patrón Saga cuando ocurre un error
 * en el proceso de reserva y es necesario deshacer las operaciones ya realizadas.</p>
 *
 * <p><strong>Nota:</strong> Esta tarea está marcada como isForCompensation=true en el BPMN,
 * por lo que solo se ejecuta cuando se dispara un evento de compensación.</p>
 *
 * @see CancelarVueloCasoUso
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CancelarVueloWorker {

    private final CancelarVueloCasoUso cancelarVueloCasoUso;

    /**
     * Procesa la tarea de cancelar una reserva de vuelo (compensación).
     *
     * <p>Variables de entrada esperadas del proceso:
     * <ul>
     *   <li>reservaVueloId (String): ID de la reserva de vuelo a cancelar</li>
     *   <li>motivoCancelacion (String, opcional): Motivo de la cancelación</li>
     * </ul>
     * </p>
     *
     * <p>Variables de salida devueltas al proceso:
     * <ul>
     *   <li>vueloCancelado (Boolean): true si la cancelación fue exitosa</li>
     *   <li>mensajeCancelacionVuelo (String): Mensaje con el resultado de la cancelación</li>
     * </ul>
     * </p>
     *
     * <p><strong>Comportamiento ante errores:</strong> Si la cancelación falla,
     * se registra el error pero NO se lanza una excepción para evitar bloquear
     * el flujo de compensación. Se retorna vueloCancelado=false.</p>
     *
     * @param job Job activado de Zeebe con las variables del proceso
     * @return Mapa con las variables de salida para el proceso
     */
    @JobWorker(type = "cancelar-vuelo", autoComplete = true)
    public Map<String, Object> cancelarVuelo(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        log.info("🛑 Iniciando worker de cancelación de vuelo (compensación) - Job Key: {}",
                job.getKey());
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Obtener ID de reserva
            String reservaVueloId = obtenerReservaVueloId(variables);

            if (reservaVueloId == null || reservaVueloId.isBlank()) {
                log.warn("⚠️ No se encontró ID de reserva de vuelo para cancelar. " +
                         "Probablemente la reserva no llegó a crearse.");

                return Map.of(
                        "vueloCancelado", true,
                        "mensajeCancelacionVuelo", "No había reserva de vuelo que cancelar"
                );
            }

            // 2. Obtener motivo de cancelación
            String motivo = (String) variables.getOrDefault("motivoCancelacion",
                    "Compensación por error en el proceso de reserva");

            log.info("❌ Cancelando reserva de vuelo: {} - Motivo: {}",
                    reservaVueloId, motivo);

            // 3. Ejecutar caso de uso de cancelación
            cancelarVueloCasoUso.ejecutar(reservaVueloId, motivo);

            log.info("✅ Reserva de vuelo cancelada exitosamente: {}", reservaVueloId);

            // 4. Preparar variables de salida
            return Map.of(
                    "vueloCancelado", true,
                    "mensajeCancelacionVuelo",
                    "Reserva de vuelo " + reservaVueloId + " cancelada correctamente"
            );

        } catch (Exception e) {
            log.error("❌ Error al cancelar reserva de vuelo: {}", e.getMessage(), e);

            // En compensaciones, NO lanzamos error para no bloquear el flujo
            // Solo registramos el fallo y retornamos false
            return Map.of(
                    "vueloCancelado", false,
                    "mensajeCancelacionVuelo",
                    "Error al cancelar vuelo: " + e.getMessage()
            );
        }
    }

    /**
     * Obtiene el ID de la reserva de vuelo desde las variables del proceso.
     * Intenta múltiples claves posibles para mayor flexibilidad.
     *
     * @param variables Variables del proceso
     * @return ID de la reserva o null si no se encuentra
     */
    private String obtenerReservaVueloId(Map<String, Object> variables) {
        // Intentar diferentes claves posibles
        Object id = variables.get("reservaVueloId");
        if (id == null) {
            id = variables.get("vueloReservaId");
        }
        if (id == null) {
            id = variables.get("idReservaVuelo");
        }

        return id != null ? id.toString() : null;
    }
}
