package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.rest;

import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.ClienteEntidad;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.repositorio.ClienteRepositorioSpringData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Utilidades de desarrollo para resetear estado de clientes bloqueados en EN_PROCESO_RESERVA
 * tras cancelar un proceso BPMN a mitad del flujo. NO exponer en producción.
 */
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Slf4j
public class DevAdminController {

    private final ClienteRepositorioSpringData clienteRepositorio;

    @GetMapping("/clientes/atascados")
    public ResponseEntity<List<Map<String, String>>> listarClientesAtascados() {
        List<Map<String, String>> atascados = clienteRepositorio
                .findByEstado(ClienteEntidad.EstadoClienteEnum.EN_PROCESO_RESERVA)
                .stream()
                .map(c -> Map.of(
                        "clienteId", c.getId(),
                        "nombre", c.getNombre() + " " + c.getApellidos(),
                        "email", c.getEmail(),
                        "estado", c.getEstado().name()
                ))
                .toList();
        log.info("🔍 [DEV] Clientes en EN_PROCESO_RESERVA: {}", atascados.size());
        return ResponseEntity.ok(atascados);
    }

    @PostMapping("/clientes/{id}/reset-estado")
    public ResponseEntity<Map<String, String>> resetEstadoCliente(@PathVariable String id) {
        return clienteRepositorio.findById(id)
                .map(cliente -> {
                    String estadoAnterior = cliente.getEstado().name();
                    cliente.setEstado(ClienteEntidad.EstadoClienteEnum.ACTIVO);
                    clienteRepositorio.save(cliente);
                    log.warn("🔧 [DEV] Estado reseteado a ACTIVO para cliente {} (era: {})", id, estadoAnterior);
                    return ResponseEntity.ok(Map.of(
                            "clienteId", id,
                            "estadoAnterior", estadoAnterior,
                            "estadoActual", "ACTIVO"
                    ));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
