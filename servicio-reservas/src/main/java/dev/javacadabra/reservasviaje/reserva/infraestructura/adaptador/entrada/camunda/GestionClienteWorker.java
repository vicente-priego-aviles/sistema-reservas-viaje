package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaViaje;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.EstadoReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker que valida si el cliente existe y está activo en el servicio-cliente.
 * Simula llamada REST al microservicio de clientes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GestionClienteWorker {

    private final ReservaRepositorioPuertoSalida repositorio;
    private final RestTemplate restTemplate;

    @JobWorker(type = "validar-cliente", autoComplete = true)
    public Map<String, Object> validarCliente(
            final ActivatedJob job,
            @Variable String reservaId,
            @Variable String clienteId) {

        log.info("🔍 Validando cliente: {} para reserva: {}", clienteId, reservaId);

        Map<String, Object> variables = new HashMap<>();

        try {
            // 1. Llamada REST al servicio de clientes (simulada)
            // TODO: Cambiar por la URL real del servicio-cliente cuando esté disponible
            String url = "http://servicio-cliente:9080/api/clientes/" + clienteId;

            boolean clienteValido = validarClienteEnServicio(clienteId, url);

            // 2. Actualizar estado en BD
            ReservaViaje reserva = repositorio.buscarPorId(new ReservaId(reservaId))
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada: " + reservaId));

            reserva.setEstado(EstadoReserva.VALIDANDO_CLIENTE);
            repositorio.guardar(reserva);

            // 3. Retornar resultado
            if (clienteValido) {
                log.info("✅ Cliente validado correctamente: {}", clienteId);
                variables.put("clienteValido", true);
                variables.put("motivoRechazoCliente", null);
            } else {
                log.warn("⚠️ Cliente no válido o inactivo: {}", clienteId);
                variables.put("clienteValido", false);
                variables.put("motivoRechazoCliente", "Cliente no existe o está inactivo");
            }

            return variables;

        } catch (Exception e) {
            log.error("❌ Error al validar cliente {}: {}", clienteId, e.getMessage());
            variables.put("clienteValido", false);
            variables.put("motivoRechazoCliente", "Error técnico al validar cliente: " + e.getMessage());
            return variables;
        }
    }

    /**
     * Valida el cliente llamando al servicio correspondiente.
     * Por ahora simula la validación.
     */
    private boolean validarClienteEnServicio(String clienteId, String url) {
        try {
            // TODO: Descomentar cuando el servicio-cliente esté disponible
            // ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            // return response.getStatusCode().is2xxSuccessful();

            // SIMULACIÓN: Validar que el clienteId no esté vacío y tenga formato correcto
            log.info("⚠️ Validación simulada de cliente (sin llamada REST real)");
            return clienteId != null && clienteId.startsWith("CLI-");

        } catch (Exception e) {
            log.error("❌ Error en llamada REST a servicio-cliente: {}", e.getMessage());
            return false;
        }
    }
}