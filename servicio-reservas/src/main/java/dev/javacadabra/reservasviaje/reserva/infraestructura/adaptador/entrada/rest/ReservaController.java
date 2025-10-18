package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.rest;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.IniciarReservaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaIniciadaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.IniciarReservaPuertoEntrada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservas", description = "API para gestionar reservas de viaje")
public class ReservaController {

    private final IniciarReservaPuertoEntrada iniciarReservaUseCase;

    @PostMapping
    @Operation(
            summary = "Iniciar nueva reserva de viaje",
            description = "Crea una nueva reserva e inicia el proceso BPMN completo en Camunda. " +
                          "Incluye validación de datos, gestión de cliente, reservas paralelas y pago."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reserva iniciada correctamente",
                    content = @Content(schema = @Schema(implementation = ReservaIniciadaDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al iniciar el proceso"
            )
    })
    public ResponseEntity<ReservaIniciadaDTO> iniciarReserva(
            @Valid @RequestBody IniciarReservaDTO dto) {

        log.info("📥 POST /api/reservas - Iniciando reserva: {} -> {}",
                dto.origen(), dto.destino());

        ReservaIniciadaDTO respuesta = iniciarReservaUseCase.iniciarReserva(dto);

        log.info("✅ Reserva iniciada: {} - Process Instance: {}",
                respuesta.reservaId(), respuesta.processInstanceKey());

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/{reservaId}")
    @Operation(summary = "Consultar estado de reserva")
    public ResponseEntity<Object> consultarReserva(@PathVariable String reservaId) {
        log.info("📥 GET /api/reservas/{}", reservaId);
        // TODO: Implementar consulta de estado
        return ResponseEntity.ok(Map.of(
                "reservaId", reservaId,
                "mensaje", "Endpoint de consulta - Implementar"
        ));
    }
}

