package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.servicio.ClienteServicio;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.EstadoCliente;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Worker de Camunda que actualiza el estado de un cliente.
 *
 * <p>Este worker es genérico y puede actualizar el estado del cliente según
 * el valor recibido en la variable de proceso {@code estadoCliente}.
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>{@code clienteId} (String) - ID del cliente a actualizar (UUID)</li>
 *   <li>{@code estadoCliente} (String) - Estado destino (EN_PROCESO_RESERVA o RESERVA_CONFIRMADA)</li>
 *   <li>{@code reservaId} (String) - ID de la reserva asociada</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>{@code estadoActualizado} (Boolean) - true si se actualizó correctamente</li>
 *   <li>{@code estadoAnterior} (String) - estado previo del cliente</li>
 *   <li>{@code estadoCliente} (String) - estado actual del cliente</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>{@code ERROR_CLIENTE_NO_ENCONTRADO} - Cliente no existe</li>
 *   <li>{@code ERROR_CLIENTE_BLOQUEADO} - Cliente está bloqueado</li>
 *   <li>{@code ERROR_CLIENTE_INACTIVO} - Cliente está inactivo</li>
 *   <li>{@code ERROR_ESTADO_INVALIDO} - Estado proporcionado no es válido</li>
 *   <li>{@code ERROR_TRANSICION_INVALIDA} - Transición de estado no permitida</li>
 *   <li>{@code ERROR_DATOS_ENTRADA} - Faltan datos obligatorios</li>
 * </ul>
 *
 * <p><strong>Transiciones de estado soportadas:</strong>
 * <ul>
 *   <li>ACTIVO → EN_PROCESO_RESERVA (iniciarProcesoReserva)</li>
 *   <li>EN_PROCESO_RESERVA → RESERVA_CONFIRMADA (confirmarReserva)</li>
 *   <li>RESERVA_CONFIRMADA → ACTIVO (finalizarReserva)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActualizarEstadoClienteWorker {

    private final ClienteServicio clienteServicio;

    /**
     * Procesa la actualización del estado del cliente.
     *
     * @param job job activado por Camunda
     * @return mapa con variables de salida
     * @throws ZeebeBpmnError si ocurre algún error durante la actualización
     */
    @JobWorker(type = "actualizar-estado-cliente", autoComplete = true)
    public Map<String, Object> actualizarEstado(ActivatedJob job) {

        log.info("🔄 Iniciando actualización de estado de cliente - Job: {}", job.getKey());

        try {
            // 1. Extraer variables de entrada
            Map<String, Object> variables = job.getVariablesAsMap();

            String clienteId = extraerClienteId(variables);
            String nuevoEstado = extraerNuevoEstado(variables);
            String reservaId = extraerReservaId(variables);

            log.info("🔍 Actualizando estado de cliente: {} → Estado nuevo: {} - Reserva: {}",
                    clienteId, nuevoEstado, reservaId);

            // 2. Validar que el estado sea válido
            validarEstado(nuevoEstado);

            // 3. Obtener estado anterior del cliente
            String estadoAnterior = clienteServicio.obtenerEstadoCliente(clienteId);

            // 4. Validar transición de estado
            validarTransicion(estadoAnterior, nuevoEstado);

            // 5. Actualizar estado según el estado destino
            actualizarSegunEstado(clienteId, nuevoEstado, reservaId);

            log.info("✅ Estado de cliente actualizado correctamente: {} - {} → {}",
                    clienteId, estadoAnterior, nuevoEstado);

            // 6. Retornar variables de salida
            return Map.of(
                    "estadoActualizado", true,
                    "estadoAnterior", estadoAnterior,
                    "estadoCliente", nuevoEstado
            );

        } catch (ClienteNoEncontradoExcepcion e) {
            log.error("❌ Cliente no encontrado: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_NO_ENCONTRADO",
                    "El cliente especificado no existe: " + e.getMessage(),
                    Map.of("estadoActualizado", false)
            );

        } catch (ClienteBloqueadoExcepcion e) {
            log.error("❌ Cliente bloqueado: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_BLOQUEADO",
                    "No se puede actualizar el estado de un cliente bloqueado: " + e.getMessage(),
                    Map.of("estadoActualizado", false)
            );

        } catch (ClienteInactivoExcepcion e) {
            log.error("❌ Cliente inactivo: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_INACTIVO",
                    "No se puede actualizar el estado de un cliente inactivo: " + e.getMessage(),
                    Map.of("estadoActualizado", false)
            );

        } catch (IllegalArgumentException e) {
            log.error("❌ Datos de entrada inválidos: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_DATOS_ENTRADA",
                    "Error en los datos de entrada: " + e.getMessage(),
                    Map.of("estadoActualizado", false)
            );

        } catch (IllegalStateException e) {
            log.error("❌ Transición de estado inválida: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_TRANSICION_INVALIDA",
                    "Transición de estado no permitida: " + e.getMessage(),
                    Map.of("estadoActualizado", false)
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al actualizar estado de cliente: {}", e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_ACTUALIZAR_ESTADO",
                    "Error inesperado al actualizar estado: " + e.getMessage(),
                    Map.of("estadoActualizado", false)
            );
        }
    }

    /**
     * Extrae el ID del cliente de las variables del proceso.
     *
     * @param variables variables del proceso
     * @return ID del cliente (UUID String)
     * @throws IllegalArgumentException si el clienteId no está presente o es inválido
     */
    private String extraerClienteId(Map<String, Object> variables) {
        if (!variables.containsKey("clienteId")) {
            throw new IllegalArgumentException("La variable 'clienteId' es obligatoria");
        }

        Object clienteIdObj = variables.get("clienteId");

        if (clienteIdObj == null) {
            throw new IllegalArgumentException("El 'clienteId' no puede ser nulo");
        }

        String clienteId = clienteIdObj.toString().trim();

        if (StringUtils.isBlank(clienteId)) {
            throw new IllegalArgumentException("El 'clienteId' no puede estar vacío");
        }

        return clienteId;
    }

    /**
     * Extrae el nuevo estado de las variables del proceso.
     *
     * @param variables variables del proceso
     * @return nuevo estado del cliente
     * @throws IllegalArgumentException si el estadoCliente no está presente o es vacío
     */
    private String extraerNuevoEstado(Map<String, Object> variables) {
        if (!variables.containsKey("nuevoEstado")) {
            throw new IllegalArgumentException("La variable 'nuevoEstado' es obligatoria");
        }

        String estadoCliente = variables.get("nuevoEstado").toString();

        if (StringUtils.isBlank(estadoCliente)) {
            throw new IllegalArgumentException("El 'estadoCliente' no puede estar vacío");
        }

        return estadoCliente.trim();
    }

    /**
     * Extrae el ID de la reserva de las variables del proceso.
     *
     * @param variables variables del proceso
     * @return ID de la reserva
     * @throws IllegalArgumentException si el reservaId no está presente o es vacío
     */
    private String extraerReservaId(Map<String, Object> variables) {
        if (!variables.containsKey("reservaId")) {
            throw new IllegalArgumentException("La variable 'reservaId' es obligatoria");
        }

        String reservaId = variables.get("reservaId").toString();

        if (StringUtils.isBlank(reservaId)) {
            throw new IllegalArgumentException("El 'reservaId' no puede estar vacío");
        }

        return reservaId.trim();
    }

    /**
     * Valida que el estado proporcionado sea un valor válido del enum EstadoCliente.
     *
     * @param estadoCliente estado a validar
     * @throws IllegalArgumentException si el estado no es válido
     */
    private void validarEstado(String estadoCliente) {
        try {
            EstadoCliente.valueOf(estadoCliente);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("El estado '%s' no es válido. Estados permitidos: %s",
                            estadoCliente,
                            String.join(", ", obtenerEstadosPermitidos()))
            );
        }
    }

    /**
     * Valida que la transición de estado sea permitida.
     *
     * <p>Transiciones permitidas:
     * <ul>
     *   <li>ACTIVO → EN_PROCESO_RESERVA</li>
     *   <li>EN_PROCESO_RESERVA → RESERVA_CONFIRMADA</li>
     *   <li>RESERVA_CONFIRMADA → ACTIVO</li>
     * </ul>
     *
     * @param estadoActual estado actual del cliente
     * @param nuevoEstado estado destino
     * @throws IllegalStateException si la transición no es permitida
     */
    private void validarTransicion(String estadoActual, String nuevoEstado) {
        boolean transicionValida = switch (estadoActual) {
            case "ACTIVO" -> "EN_PROCESO_RESERVA".equals(nuevoEstado);
            // EN_PROCESO_RESERVA → ACTIVO se permite en flujos de compensación (pago fallido)
            case "EN_PROCESO_RESERVA" -> "RESERVA_CONFIRMADA".equals(nuevoEstado) || "ACTIVO".equals(nuevoEstado);
            case "RESERVA_CONFIRMADA" -> "ACTIVO".equals(nuevoEstado);
            default -> false;
        };

        if (!transicionValida) {
            throw new IllegalStateException(
                    String.format("Transición de estado no permitida: %s → %s. " +
                                  "Consulte la documentación para ver las transiciones válidas.",
                            estadoActual, nuevoEstado)
            );
        }
    }

    /**
     * Actualiza el estado del cliente según el estado destino.
     *
     * @param clienteId ID del cliente (UUID String)
     * @param nuevoEstado estado destino
     * @param reservaId ID de la reserva
     */
    private void actualizarSegunEstado(String clienteId, String nuevoEstado, String reservaId) {
        switch (nuevoEstado) {
            case "EN_PROCESO_RESERVA" -> clienteServicio.iniciarProcesoReservaConId(clienteId, reservaId);
            case "RESERVA_CONFIRMADA" -> clienteServicio.confirmarReservaConId(clienteId, reservaId);
            case "ACTIVO" -> {
                String estadoActual = clienteServicio.obtenerEstadoCliente(clienteId);
                if ("EN_PROCESO_RESERVA".equals(estadoActual)) {
                    clienteServicio.cancelarProcesoReservaConId(clienteId, reservaId);
                } else {
                    clienteServicio.finalizarReservaConId(clienteId, reservaId);
                }
            }
            default -> throw new IllegalArgumentException("Estado no soportado: " + nuevoEstado);
        }
    }

    /**
     * Obtiene la lista de estados permitidos en este worker.
     *
     * @return array de estados permitidos
     */
    private String[] obtenerEstadosPermitidos() {
        return new String[]{"EN_PROCESO_RESERVA", "RESERVA_CONFIRMADA", "ACTIVO"};
    }
}