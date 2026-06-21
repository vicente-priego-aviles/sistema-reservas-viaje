package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.AgregarTarjetaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearTarjetaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.GestionarTarjetasUseCase;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.LimiteMaximoTarjetasExcepcion;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.exception.BpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker de Camunda para actualizar el registro del cliente (agregar tarjeta).
 *
 * <p>Este worker permite agregar una nueva tarjeta de crédito al cliente,
 * típicamente usado cuando el cliente necesita actualizar su medio de pago
 * durante o después de una reserva.
 *
 * <p><strong>Job Type:</strong> {@code actualizar-registro-cliente}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): ID del cliente</li>
 *   <li>numeroTarjeta (String): Número de la nueva tarjeta</li>
 *   <li>fechaExpiracion (String): Fecha de expiración (MM/YY)</li>
 *   <li>cvv (String): CVV de la tarjeta</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>tarjetaActualizada (Boolean): true si se agregó correctamente</li>
 *   <li>tarjetaId (String): ID de la nueva tarjeta</li>
 *   <li>cantidadTarjetas (Integer): Cantidad total de tarjetas del cliente</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>ERROR_CLIENTE_NO_ENCONTRADO: El cliente no existe</li>
 *   <li>ERROR_LIMITE_TARJETAS: El cliente ya tiene 3 tarjetas (máximo)</li>
 *   <li>ERROR_ACTUALIZAR_TARJETA: Error al agregar la tarjeta</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActualizarRegistroClienteWorker {

    private final GestionarTarjetasUseCase gestionarTarjetasUseCase;

    @JobWorker(type = "actualizar-registro-cliente", autoComplete = true)
    public Map<String, Object> manejarActualizarRegistroCliente(ActivatedJob job) {
        log.info("💳 Procesando job actualizar-registro-cliente: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String clienteId = variables.get("clienteId").toString();

        log.info("💳 Actualizando registro (agregando tarjeta) para cliente: {}", clienteId);

        try {
            // Validar variables de tarjeta
            validarVariablesTarjeta(variables);

            // Construir DTO de tarjeta
            CrearTarjetaDTO tarjetaDTO = new CrearTarjetaDTO(
                    variables.get("numeroTarjeta").toString(),
                    variables.get("fechaExpiracion").toString(),
                    variables.get("cvv").toString()
            );

            AgregarTarjetaDTO agregarTarjetaDTO = new AgregarTarjetaDTO(tarjetaDTO);

            // Agregar tarjeta al cliente
            ClienteDTO clienteActualizado = gestionarTarjetasUseCase.agregarTarjeta(
                    clienteId,
                    agregarTarjetaDTO
            );

            // Obtener ID de la última tarjeta agregada (la más reciente)
            String tarjetaId = clienteActualizado.tarjetas()
                    .get(clienteActualizado.tarjetas().size() - 1)
                    .tarjetaId();

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("tarjetaActualizada", true);
            resultado.put("tarjetaId", tarjetaId);
            resultado.put("cantidadTarjetas", clienteActualizado.cantidadTarjetas());
            resultado.put("tieneTarjetasValidas", clienteActualizado.tieneTarjetasValidas());

            log.info("✅ Tarjeta agregada al cliente: {} - Total tarjetas: {}",
                    clienteId, clienteActualizado.cantidadTarjetas());

            return resultado;

        } catch (ClienteNoEncontradoExcepcion e) {
            log.error("❌ Cliente no encontrado: {}", clienteId);
            throw BpmnError.bpmnError(
                    "ERROR_CLIENTE_NO_ENCONTRADO",
                    "Cliente no encontrado: " + clienteId,
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "CLIENTE_NO_ENCONTRADO"
                    )
            );

        } catch (LimiteMaximoTarjetasExcepcion e) {
            log.error("❌ Límite máximo de tarjetas alcanzado para cliente: {}", clienteId);
            throw BpmnError.bpmnError(
                    "ERROR_LIMITE_TARJETAS",
                    "El cliente ya tiene el máximo de tarjetas permitidas (3)",
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "LIMITE_TARJETAS",
                            "cantidadActual", 3
                    )
            );

        } catch (IllegalArgumentException e) {
            log.error("❌ Datos de tarjeta inválidos: {}", e.getMessage());
            throw BpmnError.bpmnError(
                    "ERROR_VALIDACION_TARJETA",
                    "Datos de tarjeta inválidos: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "VALIDACION_TARJETA",
                            "mensajeError", e.getMessage()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Error al actualizar registro del cliente {}: {}",
                    clienteId, e.getMessage(), e);
            throw BpmnError.bpmnError(
                    "ERROR_ACTUALIZAR_TARJETA",
                    "Error al actualizar tarjeta: " + e.getMessage(),
                    Map.of(
                            "clienteId", clienteId,
                            "errorType", "ERROR_INESPERADO",
                            "mensajeError", e.getMessage()
                    )
            );
        }
    }

    /**
     * Valida que todas las variables de tarjeta estén presentes.
     */
    private void validarVariablesTarjeta(Map<String, Object> variables) {
        String[] camposObligatorios = {"numeroTarjeta", "fechaExpiracion", "cvv"};

        for (String campo : camposObligatorios) {
            if (!variables.containsKey(campo) || variables.get(campo) == null) {
                throw new IllegalArgumentException("Campo obligatorio faltante: " + campo);
            }
        }
    }
}