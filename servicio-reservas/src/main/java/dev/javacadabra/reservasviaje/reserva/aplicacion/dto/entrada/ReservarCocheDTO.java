package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de entrada para crear una reserva de coche de alquiler.
 * Contiene todos los datos necesarios para reservar un coche.
 */
public record ReservarCocheDTO(

        @NotBlank(message = "El ID del cliente es obligatorio")
        String clienteId,

        @NotBlank(message = "La empresa de alquiler es obligatoria")
        String empresaAlquiler,

        @NotBlank(message = "El modelo del coche es obligatorio")
        String modeloCoche,

        @NotBlank(message = "La categoría del coche es obligatoria")
        String categoriaCoche,

        @NotBlank(message = "La ubicación de recogida es obligatoria")
        String ubicacionRecogida,

        @NotBlank(message = "La ubicación de devolución es obligatoria")
        String ubicacionDevolucion,

        @NotNull(message = "La fecha de recogida es obligatoria")
        @Future(message = "La fecha de recogida debe ser futura")
        LocalDateTime fechaRecogida,

        @NotNull(message = "La fecha de devolución es obligatoria")
        @Future(message = "La fecha de devolución debe ser futura")
        LocalDateTime fechaDevolucion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
        BigDecimal precio,

        String codigoMoneda,

        String observaciones,

        String codigoConfirmacion
) {}
