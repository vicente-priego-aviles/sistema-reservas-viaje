package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.ConsultarClienteUseCase;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.exception.BpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker de Camunda para obtener los datos completos de un cliente.
 *
 * <p>Este worker se ejecuta en el subproceso de gestión de cliente y es responsable
 * de consultar todos los datos del cliente en el sistema, incluyendo su información
 * personal, dirección, estado y tarjetas de crédito asociadas.
 *
 * <p><strong>Job Type:</strong> {@code obtener-datos-cliente}
 *
 * <p><strong>BPMN:</strong> {@code subproceso-gestion-cliente.bpmn}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): Identificador único del cliente</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>clienteObtenido (Boolean): true si el cliente existe, false si no</li>
 *   <li>estadoCliente (String): Estado actual del cliente (ACTIVO, BLOQUEADO, etc.)</li>
 *   <li>cantidadTarjetas (Integer): Número de tarjetas de crédito asociadas</li>
 *   <li>emailCliente (String): Email del cliente</li>
 *   <li>nombreCompleto (String): Nombre completo del cliente</li>
 *   <li>estaBloqueado (Boolean): true si el cliente está bloqueado</li>
 *   <li>puedeRealizarPagos (Boolean): true si puede realizar Pagos</li>
 *   <li>tieneTarjetasValidas (Boolean): true si tiene tarjetas válidas</li>
 * </ul>
 *
 * <p><strong>Flujo en el BPMN:</strong>
 * <pre>
 * Inicio → [obtener-datos-cliente] → Gateway (¿Cliente encontrado?)
 *    ├─ [Sí] → validar-tarjeta-credito → ...
 *    └─ [No] → Error: Cliente No Encontrado
 * </pre>
 *
 * <p><strong>Comportamiento:</strong>
 * <ul>
 *   <li>Si el cliente existe: devuelve sus datos completos con clienteObtenido=true</li>
 *   <li>Si el cliente NO existe: devuelve clienteObtenido=false (permite que el gateway BPMN maneje el flujo)</li>
 *   <li>En caso de error inesperado: lanza BpmnError para manejo en el BPMN</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 * @see ConsultarClienteUseCase
 * @see ClienteDTO
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ObtenerDatosClienteWorker {

    private final ConsultarClienteUseCase consultarClienteUseCase;

    /**
     * Maneja el job de obtener datos del cliente desde Camunda.
     *
     * <p>Este método busca el cliente por su ID y devuelve toda su información
     * al proceso BPMN. Si el cliente no existe, devuelve clienteObtenido=false
     * para que el gateway exclusivo del BPMN tome la decisión de flujo.
     *
     * @param job job activado por Zeebe con las variables del proceso
     * @return mapa con las variables de salida para el proceso BPMN
     * @throws BpmnError si ocurre un error inesperado al consultar el cliente
     */
    @JobWorker(type = "obtener-datos-cliente", autoComplete = true)
    public Map<String, Object> manejarObtenerDatosCliente(ActivatedJob job) {
        log.info("🚀 Iniciando worker obtener-datos-cliente - Job Key: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Validar y extraer clienteId
            String clienteId = validarYExtraerClienteId(variables);

            log.info("🔍 Obteniendo datos del cliente: {}", clienteId);

            // 2. Buscar cliente en el sistema
            try {
                ClienteDTO cliente = consultarClienteUseCase.buscarPorId(clienteId);

                log.info("✅ Cliente encontrado: {} - Estado: {} - Email: {}",
                        clienteId,
                        cliente.estado(),
                        cliente.datosPersonales().email());

                // 3. Preparar variables de salida con datos del cliente
                Map<String, Object> output = construirOutputConCliente(cliente);

                log.info("📤 Datos del cliente preparados - Tarjetas: {} - Puede reservar: {}",
                        cliente.cantidadTarjetas(),
                        cliente.puedeRealizarPagos());

                return output;

            } catch (ClienteNoEncontradoExcepcion e) {
                log.warn("⚠️ Cliente no encontrado en el sistema: {}", clienteId);

                // Devolver clienteObtenido=false para que el gateway BPMN maneje el flujo
                Map<String, Object> output = new HashMap<>();
                output.put("clienteObtenido", false);
                output.put("clienteId", clienteId);
                output.put("mensajeError", "Cliente no encontrado con ID: " + clienteId);

                log.info("📤 Respuesta preparada: clienteObtenido=false");

                return output;
            }

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al obtener datos del cliente: {}", e.getMessage());
            throw BpmnError.bpmnError(
                    "ERROR_VALIDACION_CLIENTE",
                    "Error de validación: " + e.getMessage(),
                    Map.of(
                            "errorType", "VALIDACION",
                            "mensajeError", e.getMessage()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al obtener datos del cliente: {}",
                    e.getMessage(), e);
            throw BpmnError.bpmnError(
                    "ERROR_OBTENER_CLIENTE",
                    "Error al obtener datos del cliente: " + e.getMessage(),
                    Map.of(
                            "errorType", "ERROR_SISTEMA",
                            "mensajeError", e.getMessage()
                    )
            );
        }
    }

    /**
     * Valida que el clienteId esté presente y sea válido.
     *
     * @param variables variables del job de Camunda
     * @return clienteId validado
     * @throws IllegalArgumentException si el clienteId es inválido
     */
    private String validarYExtraerClienteId(Map<String, Object> variables) {
        Object clienteIdObj = variables.get("clienteId");

        if (clienteIdObj == null) {
            throw new IllegalArgumentException(
                    "La variable 'clienteId' es obligatoria pero no está presente"
            );
        }

        String clienteId = clienteIdObj.toString().trim();

        if (StringUtils.isBlank(clienteId)) {
            throw new IllegalArgumentException(
                    "El clienteId no puede estar vacío"
            );
        }

        log.debug("✅ ClienteId validado: {}", clienteId);
        return clienteId;
    }

    /**
     * Construye el mapa de variables de salida con todos los datos del cliente.
     *
     * @param cliente DTO con los datos del cliente
     * @return mapa con las variables de salida para Camunda
     */
    private Map<String, Object> construirOutputConCliente(ClienteDTO cliente) {
        Map<String, Object> output = new HashMap<>();

        // Variable principal de control de flujo
        output.put("clienteObtenido", true);

        // Identificación del cliente
        output.put("clienteId", cliente.clienteId());
        output.put("emailCliente", cliente.datosPersonales().email());
        output.put("nombreCompleto", cliente.datosPersonales().nombreCompleto());

        // Estado del cliente
        output.put("estadoCliente", cliente.estado());
        output.put("estaBloqueado", cliente.estaBloqueado());
        output.put("puedeRealizarPagos", cliente.puedeRealizarPagos());

        // Información de tarjetas
        output.put("cantidadTarjetas", cliente.cantidadTarjetas());
        output.put("tieneTarjetasValidas", cliente.tieneTarjetasValidas());

        // Información adicional útil para el proceso
        output.put("dniCliente", cliente.datosPersonales().dniEnmascarado());
        output.put("telefonoCliente", cliente.datosPersonales().telefono() != null
                ? cliente.datosPersonales().telefono()
                : "");

        // Dirección (opcional, por si se necesita para notificaciones)
        if (cliente.direccion() != null) {
            output.put("ciudadCliente", cliente.direccion().ciudad());
            output.put("paisCliente", cliente.direccion().pais());
        }

        // Motivo de bloqueo (si aplica)
        if (cliente.estaBloqueado() && cliente.motivoBloqueo() != null) {
            output.put("motivoBloqueo", cliente.motivoBloqueo());
        }

        log.debug("✅ Output construido con {} variables", output.size());

        return output;
    }
}
