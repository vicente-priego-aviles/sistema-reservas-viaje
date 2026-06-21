package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.rest;

import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.ClienteEntidad;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.repositorio.ClienteRepositorioSpringData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
