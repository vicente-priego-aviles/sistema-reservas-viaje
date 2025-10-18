package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta al iniciar una reserva")
public record ReservaIniciadaDTO(

        @Schema(description = "ID de la reserva generado", example = "550e8400-e29b-41d4-a716-446655440000")
        String reservaId,

        @Schema(description = "Clave de la instancia del proceso en Camunda", example = "2251799813685249")
        Long processInstanceKey,

        @Schema(description = "Estado actual de la reserva", example = "INICIADA")
        String estado,

        @Schema(description = "Fecha y hora de creación")
        LocalDateTime fechaCreacion,

        @Schema(description = "Mensaje informativo")
        String mensaje
) {}
