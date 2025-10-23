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
 * Worker de Camunda para confirmar una reserva de cliente.
 *
 * <p>Este worker cambia el estado del cliente de EN_PROCESO_RESERVA a RESERVA_CONFIRMADA
 * tras un pago exitoso.
 *
 * <p><strong>Job Type:</strong> {@code confirmar-reserva-cliente}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): ID del cliente</li>
 *   <li>reservaId (String): ID de la reserva confirmada</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>reservaConfirmada (Boolean): true si se confirmó correctamente</li>
 *   <li>estadoCliente (String): Estado actualizado (RESERVA_CONFIRMADA)</li>
 *   <li>fechaConfirmacion (String): Fecha/hora de confirmación</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmarReservaWorker {

    private final GestionarEstadoClienteUseCase gestionarEstadoClienteUseCase;

    @JobWorker(type = "confirmar-reserva-cliente", autoComplete = true)
    public Map<String, Object> manejarConfirmarReserva(ActivatedJob job) {
        log.info("✅ Procesando job confirmar-reserva-cliente: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String clienteId = variables.get("clienteId").toString();
        String reservaId = variables.get("reservaId").toString();

        log.info("✅ Confirmando reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        try {
            ClienteDTO clienteActualizado = gestionarEstadoClienteUseCase.confirmarReserva(clienteId);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("reservaConfirmada", true);
            resultado.put("estadoCliente", clienteActualizado.estado());
            resultado.put("fechaConfirmacion", java.time.LocalDateTime.now().toString());

            log.info("✅ Reserva confirmada para cliente: {} - Reserva: {}", clienteId, reservaId);

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
            log.error("❌ Estado inválido para confirmar reserva en cliente {}: {}",
                    clienteId, e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_ESTADO_INVALIDO",
                    "El cliente no puede confirmar reserva en su estado actual: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "reservaId", reservaId,
                            "errorType", "ESTADO_INVALIDO",
                            "mensajeError", e.getMessage()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Error al confirmar reserva para cliente {}: {}",
                    clienteId, e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_CONFIRMAR_RESERVA",
                    "Error al confirmar reserva: " + e.getMessage(),
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
