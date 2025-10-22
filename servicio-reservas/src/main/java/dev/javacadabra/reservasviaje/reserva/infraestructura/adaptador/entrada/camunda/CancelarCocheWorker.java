package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.CancelarCocheCasoUso;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Worker de Camunda para procesar la compensación de reserva de coche.
 * Escucha el task type "cancelar-coche" del proceso BPMN (tarea de compensación)
 * y ejecuta la lógica de cancelación cuando se necesita revertir una reserva.
 *
 * <p>Este worker se ejecuta como parte del patrón Saga cuando ocurre un error
 * en el proceso de reserva y es necesario deshacer las operaciones ya realizadas.</p>
 *
 * <p><strong>Nota:</strong> Esta tarea está marcada como isForCompensation=true en el BPMN,
 * por lo que solo se ejecuta cuando se dispara un evento de compensación.</p>
 *
 * @see CancelarCocheCasoUso
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CancelarCocheWorker {

    private final CancelarCocheCasoUso cancelarCocheCasoUso;

    /**
     * Procesa la tarea de cancelar una reserva de coche (compensación).
     *
     * <p>Variables de entrada esperadas del proceso:
     * <ul>
     *   <li>reservaCocheId (String): ID de la reserva de coche a cancelar</li>
     *   <li>motivoCancelacion (String, opcional): Motivo de la cancelación</li>
     * </ul>
     * </p>
     *
     * <p>Variables de salida devueltas al proceso:
     * <ul>
     *   <li>cocheCancelado (Boolean): true si la cancelación fue exitosa</li>
     *   <li>mensajeCancelacionCoche (String): Mensaje con el resultado de la cancelación</li>
     * </ul>
     * </p>
     *
     * <p><strong>Comportamiento ante errores:</strong> Si la cancelación falla,
     * se registra el error pero NO se lanza una excepción para evitar bloquear
     * el flujo de compensación. Se retorna cocheCancelado=false.</p>
     *
     * @param job Job activado de Zeebe con las variables del proceso
     * @return Mapa con las variables de salida para el proceso
     */
    @JobWorker(type = "cancelar-coche", autoComplete = true)
    public Map<String, Object> cancelarCoche(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        log.info("🛑 Iniciando worker de cancelación de coche (compensación) - Job Key: {}",
                job.getKey());
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Obtener ID de reserva
            String reservaCocheId = obtenerReservaCocheId(variables);

            if (reservaCocheId == null || reservaCocheId.isBlank()) {
                log.warn("⚠️ No se encontró ID de reserva de coche para cancelar. " +
                         "Probablemente la reserva no llegó a crearse.");

                return Map.of(
                        "cocheCancelado", true,
                        "mensajeCancelacionCoche", "No había reserva de coche que cancelar"
                );
            }

            // 2. Obtener motivo de cancelación
            String motivo = (String) variables.getOrDefault("motivoCancelacion",
                    "Compensación por error en el proceso de reserva");

            log.info("❌ Cancelando reserva de coche: {} - Motivo: {}",
                    reservaCocheId, motivo);

            // 3. Ejecutar caso de uso de cancelación
            cancelarCocheCasoUso.ejecutar(reservaCocheId, motivo);

            log.info("✅ Reserva de coche cancelada exitosamente: {}", reservaCocheId);

            // 4. Preparar variables de salida
            return Map.of(
                    "cocheCancelado", true,
                    "mensajeCancelacionCoche",
                    "Reserva de coche " + reservaCocheId + " cancelada correctamente"
            );

        } catch (Exception e) {
            log.error("❌ Error al cancelar reserva de coche: {}", e.getMessage(), e);

            // En compensaciones, NO lanzamos error para no bloquear el flujo
            // Solo registramos el fallo y retornamos false
            return Map.of(
                    "cocheCancelado", false,
                    "mensajeCancelacionCoche",
                    "Error al cancelar coche: " + e.getMessage()
            );
        }
    }

    /**
     * Obtiene el ID de la reserva de coche desde las variables del proceso.
     * Intenta múltiples claves posibles para mayor flexibilidad.
     *
     * @param variables Variables del proceso
     * @return ID de la reserva o null si no se encuentra
     */
    private String obtenerReservaCocheId(Map<String, Object> variables) {
        // Intentar diferentes claves posibles
        Object id = variables.get("reservaCocheId");
        if (id == null) {
            id = variables.get("cocheReservaId");
        }
        if (id == null) {
            id = variables.get("idReservaCoche");
        }

        return id != null ? id.toString() : null;
    }
}

