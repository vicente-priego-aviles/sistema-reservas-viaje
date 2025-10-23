package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.GestionarEstadoClienteUseCase;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker de Camunda para finalizar el proceso de reserva de un cliente.
 *
 * <p>Este worker cambia el estado del cliente de RESERVA_CONFIRMADA a ACTIVO,
 * completando el ciclo de la reserva.
 *
 * <p><strong>Job Type:</strong> {@code finalizar-reserva-cliente}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): ID del cliente</li>
 *   <li>reservaId (String): ID de la reserva finalizada</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>reservaFinalizada (Boolean): true si se finalizó correctamente</li>
 *   <li>estadoCliente (String): Estado actualizado (ACTIVO)</li>
 *   <li>fechaFinalizacion (String): Fecha/hora de finalización</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FinalizarReservaWorker {

    private final GestionarEstadoClienteUseCase gestionarEstadoClienteUseCase;

    @JobWorker(type = "finalizar-reserva-cliente", autoComplete = true)
    public Map<String, Object> manejarFinalizarReserva(ActivatedJob job) {
        log.info("🏁 Procesando job finalizar-reserva-cliente: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String clienteId = variables.get("clienteId").toString();
        String reservaId = variables.get("reservaId").toString();

        log.info("🏁 Finalizando reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        try {
            ClienteDTO clienteActualizado = gestionarEstadoClienteUseCase.finalizarReserva(clienteId);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("reservaFinalizada", true);
            resultado.put("estadoCliente", clienteActualizado.estado());
            resultado.put("fechaFinalizacion", java.time.LocalDateTime.now().toString());
            resultado.put("puedeRealizarReservas", clienteActualizado.puedeRealizarReservas());

            log.info("✅ Reserva finalizada para cliente: {} - Reserva: {}", clienteId, reservaId);

            return resultado;

        } catch (ClienteNoEncontradoExcepcion e) {
            log.error("❌ Cliente no encontrado: {}", clienteId);
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_NO_ENCONTRADO",
                    "Cliente no encontrado: " + clienteId,
                    Map.of(
                            "clienteId", clienteId,
                            "reservaId", reservaId,
                            "errorType", "CLIENTE_NO_ENCONTRADO"
                    )
            );

        } catch (IllegalStateException e) {
            log.error("❌ Estado inválido para finalizar reserva en cliente {}: {}",
                    clienteId, e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_ESTADO_INVALIDO",
                    "El cliente no puede finalizar reserva en su estado actual: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "reservaId", reservaId,
                            "errorType", "ESTADO_INVALIDO",
                            "mensajeError", e.getMessage()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Error al finalizar reserva para cliente {}: {}",
                    clienteId, e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_FINALIZAR_RESERVA",
                    "Error al finalizar reserva: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "reservaId", reservaId,
                            "errorType", "ERROR_INESPERADO",
                            "mensajeError", e.getMessage()
                    )
            );
        }
    }
}
