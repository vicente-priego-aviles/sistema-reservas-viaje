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
 * Worker de Camunda para activar un cliente.
 *
 * <p>Este worker cambia el estado del cliente de PENDIENTE_VALIDACION a ACTIVO,
 * permitiéndole realizar reservas en el sistema.
 *
 * <p><strong>Job Type:</strong> {@code activar-cliente}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): ID del cliente a activar</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>clienteActivado (Boolean): true si se activó exitosamente</li>
 *   <li>estadoCliente (String): Estado actualizado del cliente (ACTIVO)</li>
 *   <li>puedeRealizarReservas (Boolean): true si puede hacer reservas</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>ERROR_CLIENTE_NO_ENCONTRADO: El cliente no existe</li>
 *   <li>ERROR_ESTADO_INVALIDO: El cliente no está en estado PENDIENTE_VALIDACION</li>
 *   <li>ERROR_ACTIVACION_CLIENTE: Error inesperado al activar</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivarClienteWorker {

    private final GestionarEstadoClienteUseCase gestionarEstadoClienteUseCase;

    /**
     * Maneja el job de activar cliente desde Camunda.
     *
     * @param job job activado por Zeebe
     * @return variables de salida para el proceso BPMN
     */
    @JobWorker(type = "activar-cliente", autoComplete = true)
    public Map<String, Object> manejarActivarCliente(ActivatedJob job) {
        log.info("✅ Procesando job activar-cliente: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String clienteId = variables.get("clienteId").toString();

        log.info("✅ Activando cliente: {}", clienteId);

        try {
            // Activar cliente
            ClienteDTO clienteActivado = gestionarEstadoClienteUseCase.activarCliente(clienteId);

            // Preparar variables de salida
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("clienteActivado", true);
            resultado.put("estadoCliente", clienteActivado.estado());
            resultado.put("puedeRealizarReservas", clienteActivado.puedeRealizarReservas());
            resultado.put("fechaActivacion", java.time.LocalDateTime.now().toString());

            log.info("✅ Cliente activado exitosamente: {} - Estado: {}",
                    clienteId, clienteActivado.estado());

            return resultado;

        } catch (ClienteNoEncontradoExcepcion e) {
            log.error("❌ Cliente no encontrado: {}", clienteId);
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_NO_ENCONTRADO",
                    "Cliente no encontrado: " + clienteId,
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "CLIENTE_NO_ENCONTRADO"
                    )
            );

        } catch (IllegalStateException e) {
            log.error("❌ Estado inválido para activar cliente {}: {}", clienteId, e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_ESTADO_INVALIDO",
                    "No se puede activar el cliente en su estado actual: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "ESTADO_INVALIDO",
                            "mensajeError", e.getMessage()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Error al activar cliente {}: {}", clienteId, e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_ACTIVACION_CLIENTE",
                    "Error al activar cliente: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "ERROR_ACTIVACION",
                            "mensajeError", e.getMessage()
                    )
            );
        }
    }
}