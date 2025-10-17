package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.rest;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ClienteEntradaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteSalidaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "API de gestión de clientes")
public class ClienteController {

    private final ObtenerClientePuertoEntrada obtenerClienteUseCase;
    private final CrearClientePuertoEntrada crearClienteUseCase;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ClienteSalidaDTO> obtenerCliente(@PathVariable String id) {
        log.info("📥 GET /api/clientes/{}", id);
        ClienteSalidaDTO cliente = obtenerClienteUseCase.obtenerCliente(id);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo cliente")
    public ResponseEntity<ClienteSalidaDTO> crearCliente(
            @Valid @RequestBody ClienteEntradaDTO dto) {
        log.info("📥 POST /api/clientes");
        ClienteSalidaDTO cliente = crearClienteUseCase.crearCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    @GetMapping
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<ClienteSalidaDTO>> listarClientes() {
        log.info("📥 GET /api/clientes");
        // Implementar...
        return ResponseEntity.ok(List.of());
    }
}