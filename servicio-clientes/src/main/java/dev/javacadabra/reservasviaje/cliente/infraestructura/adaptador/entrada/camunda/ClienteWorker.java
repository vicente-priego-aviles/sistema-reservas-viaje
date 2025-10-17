package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteSalidaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.valorobjeto.EstadoCliente;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteWorker {

    private final ObtenerClientePuertoEntrada obtenerClienteUseCase;
    private final ValidarTarjetaPuertoEntrada validarTarjetaUseCase;
    private final ActualizarEstadoPuertoEntrada actualizarEstadoUseCase;

    @JobWorker(type = "obtener-datos-cliente")
    public Map<String, Object> obtenerDatosCliente(ActivatedJob job) {
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");

        log.info("🔄 Worker: obtener-datos-cliente para cliente: {}", clienteId);

        try {
            ClienteSalidaDTO cliente = obtenerClienteUseCase.obtenerCliente(clienteId);

            log.info("✅ Cliente obtenido: {}", cliente.nombre());

            return Map.of(
                    "clienteObtenido", true,
                    "clienteNombre", cliente.nombre(),
                    "clienteEmail", cliente.email()
            );
        } catch (ClienteNoEncontradoException e) {
            log.error("❌ Cliente no encontrado: {}", clienteId);
            return Map.of("clienteObtenido", false);
        }
    }

    @JobWorker(type = "validar-tarjeta-credito")
    public Map<String, Object> validarTarjeta(ActivatedJob job) {
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");

        log.info("🔄 Worker: validar-tarjeta-credito para cliente: {}", clienteId);

        try {
            validarTarjetaUseCase.validarTarjeta(clienteId);
            log.info("✅ Tarjeta validada correctamente");
            return Map.of("tarjetaValida", true);
        } catch (TarjetaInvalidaException e) {
            log.error("❌ Tarjeta inválida para cliente: {}", clienteId);
            throw new ZeebeBpmnError("ERROR_TARJETA_INVALIDA", e.getMessage());
        }
    }

    @JobWorker(type = "actualizar-estado-cliente")
    public void actualizarEstado(ActivatedJob job) {
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");
        String nuevoEstadoStr = (String) job.getVariablesAsMap().get("nuevoEstado");

        log.info("🔄 Worker: actualizar-estado-cliente {} a {}", clienteId, nuevoEstadoStr);

        EstadoCliente nuevoEstado = EstadoCliente.valueOf(nuevoEstadoStr);
        actualizarEstadoUseCase.actualizarEstado(clienteId, nuevoEstado);

        log.info("✅ Estado actualizado correctamente");
    }

    @JobWorker(type = "revertir-estado-cliente")
    public void revertirEstado(ActivatedJob job) {
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");

        log.info("🔄 Worker: revertir-estado-cliente para {}", clienteId);

        actualizarEstadoUseCase.actualizarEstado(clienteId, EstadoCliente.ACTIVO);

        log.info("✅ Estado revertido a ACTIVO");
    }
}