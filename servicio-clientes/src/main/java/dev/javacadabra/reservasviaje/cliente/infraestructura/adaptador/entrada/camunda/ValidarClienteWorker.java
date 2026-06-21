package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.ConsultarClienteUseCase;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.exception.BpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker de Camunda para validar los datos de un cliente.
 *
 * <p>Este worker simula un proceso de validación de cliente, verificando
 * que el cliente existe, tiene datos correctos y cumple requisitos mínimos.
 *
 * <p><strong>Job Type:</strong> {@code validar-cliente}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): ID del cliente a validar</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>clienteValido (Boolean): true si el cliente es válido</li>
 *   <li>motivoRechazo (String, opcional): motivo si el cliente no es válido</li>
 *   <li>validacionCompletada (Boolean): true cuando termina la validación</li>
 * </ul>
 *
 * <p><strong>Criterios de validación:</strong>
 * <ul>
 *   <li>El cliente debe existir en el sistema</li>
 *   <li>Debe tener al menos una tarjeta de crédito</li>
 *   <li>La tarjeta debe ser válida (no expirada)</li>
 *   <li>El cliente no debe estar bloqueado</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidarClienteWorker {

    private final ConsultarClienteUseCase consultarClienteUseCase;

    /**
     * Maneja el job de validar cliente desde Camunda.
     *
     * @param job job activado por Zeebe
     * @return variables de salida para el proceso BPMN
     */
    @JobWorker(type = "validar-cliente", autoComplete = true)
    public Map<String, Object> manejarValidarCliente(ActivatedJob job) {
        log.info("🔍 Procesando job validar-cliente: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String clienteId = variables.get("clienteId").toString();

        log.info("🔍 Validando cliente: {}", clienteId);

        try {
            // Consultar cliente
            ClienteDTO cliente = consultarClienteUseCase.buscarPorId(clienteId);

            // Ejecutar validaciones
            ResultadoValidacion resultado = ejecutarValidaciones(cliente);

            // Preparar variables de salida
            Map<String, Object> output = new HashMap<>();
            output.put("clienteValido", resultado.esValido());
            output.put("motivoRechazo", resultado.motivoRechazo());
            output.put("validacionCompletada", true);
            output.put("estadoCliente", cliente.estado());

            if (resultado.esValido()) {
                log.info("✅ Cliente validado correctamente: {}", clienteId);
            } else {
                log.warn("⚠️ Cliente NO validado: {} - Motivo: {}",
                        clienteId, resultado.motivoRechazo());
            }

            return output;

        } catch (ClienteNoEncontradoExcepcion e) {
            log.error("❌ Cliente no encontrado: {}", clienteId);

            Map<String, Object> output = new HashMap<>();
            output.put("clienteValido", false);
            output.put("motivoRechazo", "Cliente no encontrado en el sistema");
            output.put("validacionCompletada", true);

            return output;

        } catch (Exception e) {
            log.error("❌ Error al validar cliente {}: {}", clienteId, e.getMessage(), e);
            throw BpmnError.bpmnError(
                    "ERROR_VALIDACION_CLIENTE",
                    "Error al validar cliente: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "ERROR_VALIDACION",
                            "mensajeError", e.getMessage()
                    )
            );
        }
    }

    /**
     * Ejecuta todas las validaciones del cliente.
     *
     * @param cliente cliente a validar
     * @return resultado de la validación
     */
    private ResultadoValidacion ejecutarValidaciones(ClienteDTO cliente) {
        // Validación 1: Cliente no debe estar bloqueado
        if (cliente.estaBloqueado()) {
            return ResultadoValidacion.rechazado(
                    "Cliente bloqueado: " +
                    (cliente.motivoBloqueo() != null ? cliente.motivoBloqueo() : "Motivo no especificado")
            );
        }

        // Validación 2: Cliente debe tener al menos una tarjeta
        if (cliente.cantidadTarjetas() == 0) {
            return ResultadoValidacion.rechazado(
                    "El cliente no tiene tarjetas de crédito registradas"
            );
        }

        // Validación 3: Cliente debe tener al menos una tarjeta válida
        if (!cliente.tieneTarjetasValidas()) {
            return ResultadoValidacion.rechazado(
                    "El cliente no tiene tarjetas de crédito válidas (todas expiradas o rechazadas)"
            );
        }

        // Validación 4: Cliente debe estar en estado que permita Pagos
        if (!cliente.puedeRealizarPagos()) {
            return ResultadoValidacion.rechazado(
                    "El cliente no puede realizar Pagos en su estado actual: " + cliente.estado()
            );
        }

        // Todas las validaciones pasaron
        return ResultadoValidacion.aprobado();
    }

    /**
     * Record para encapsular el resultado de la validación.
     */
    private record ResultadoValidacion(boolean esValido, String motivoRechazo) {

        static ResultadoValidacion aprobado() {
            return new ResultadoValidacion(true, null);
        }

        static ResultadoValidacion rechazado(String motivo) {
            return new ResultadoValidacion(false, motivo);
        }
    }
}