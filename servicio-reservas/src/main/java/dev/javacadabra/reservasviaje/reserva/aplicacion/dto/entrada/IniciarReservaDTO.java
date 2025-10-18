package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Datos para iniciar una reserva de viaje")
public record IniciarReservaDTO(

        @NotBlank(message = "El ID del cliente es obligatorio")
        @Schema(description = "ID del cliente", example = "CLI-001")
        String clienteId,

        @NotBlank(message = "El origen es obligatorio")
        @Schema(description = "Ciudad de origen", example = "Madrid")
        String origen,

        @NotBlank(message = "El destino es obligatorio")
        @Schema(description = "Ciudad de destino", example = "Barcelona")
        String destino,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @Future(message = "La fecha de inicio debe ser futura")
        @Schema(description = "Fecha de inicio del viaje", example = "2025-12-15")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        @Future(message = "La fecha de fin debe ser futura")
        @Schema(description = "Fecha de fin del viaje", example = "2025-12-20")
        LocalDate fechaFin,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser positivo")
        @Schema(description = "Monto total de la reserva", example = "1500.00")
        Double monto
) {

    public IniciarReservaDTO {
        // Validación adicional
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }
}