package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.GestionarEstadoClienteUseCase;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion;
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
 * Worker de Camunda para iniciar el proceso de reserva de un cliente.
 *
 * <p>Este worker cambia el estado del cliente de ACTIVO a EN_PROCESO_RESERVA,
 * marcando que el cliente tiene una reserva en curso.
 *
 * <p><strong>Job Type:</strong> {@code iniciar-reserva-cliente}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): ID del cliente</li>
 *   <li>reservaId (String): ID de la reserva que se está iniciando</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>reservaIniciada (Boolean): true si se inició correctamente</li>
 *   <li>estadoCliente (String): Estado actualizado (EN_PROCESO_RESERVA)</li>
 *   <li>fechaInicioReserva (String): Fecha/hora de inicio</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>ERROR_CLIENTE_NO_ENCONTRADO: El cliente no existe</li>
 *   <li>ERROR_CLIENTE_BLOQUEADO: El cliente está bloqueado</li>
 *   <li>ERROR_CLIENTE_INACTIVO: El cliente está inactivo</li>
 *   <li>ERROR_ESTADO_INVALIDO: El cliente no está en estado ACTIVO</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IniciarReservaWorker {

    private final GestionarEstadoClienteUseCase gestionarEstadoClienteUseCase;

    /**
     * Maneja el job de iniciar reserva desde Camunda.
     *
     * @param job job activado por Zeebe
     * @return variables de salida para el proceso BPMN
     */
    @JobWorker(type = "iniciar-reserva-cliente", autoComplete = true)
    public Map<String, Object> manejarIniciarReserva(ActivatedJob job) {
        log.info("🚀 Procesando job iniciar-reserva-cliente: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String clienteId = variables.get("clienteId").toString();
        String reservaId = variables.get("reservaId").toString();

        log.info("🚀 Iniciando proceso de reserva para cliente: {} - Reserva: {}",
                clienteId, reservaId);

        try {
            // Iniciar proceso de reserva
            ClienteDTO clienteActualizado = gestionarEstadoClienteUseCase.iniciarProcesoReserva(clienteId);

            // Preparar variables de salida
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("reservaIniciada", true);
            resultado.put("estadoCliente", clienteActualizado.estado());
            resultado.put("fechaInicioReserva", java.time.LocalDateTime.now().toString());

            log.info("✅ Proceso de reserva iniciado para cliente: {} - Reserva: {}",
                    clienteId, reservaId);

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

        } catch (ClienteBloqueadoExcepcion e) {
            log.error("❌ Cliente bloqueado: {} - Motivo: {}", clienteId, e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_BLOQUEADO",
                    "Cliente bloqueado: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "reservaId", reservaId,
                            "errorType", "CLIENTE_BLOQUEADO",
                            "motivoBloqueo", e.getMessage()
                    )
            );

        } catch (ClienteInactivoExcepcion e) {
            log.error("❌ Cliente inactivo: {}", clienteId);
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_INACTIVO",
                    "Cliente inactivo: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "reservaId", reservaId,
                            "errorType", "CLIENTE_INACTIVO"
                    )
            );

        } catch (IllegalStateException e) {
            log.error("❌ Estado inválido para iniciar reserva en cliente {}: {}",
                    clienteId, e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_ESTADO_INVALIDO",
                    "El cliente no puede iniciar una reserva en su estado actual: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "reservaId", reservaId,
                            "errorType", "ESTADO_INVALIDO",
                            "mensajeError", e.getMessage()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Error al iniciar reserva para cliente {}: {}",
                    clienteId, e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_INICIAR_RESERVA",
                    "Error al iniciar proceso de reserva: " + e.getMessage(),
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