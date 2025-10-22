package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para crear una reserva de hotel.
 * Contiene todos los datos necesarios para reservar un hotel.
 */
public record ReservarHotelDTO(

        @NotBlank(message = "El ID del cliente es obligatorio")
        String clienteId,

        @NotBlank(message = "El nombre del hotel es obligatorio")
        String nombreHotel,

        @NotBlank(message = "La ciudad es obligatoria")
        String ciudad,

        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        @NotNull(message = "La fecha de entrada es obligatoria")
        @Future(message = "La fecha de entrada debe ser futura")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        @Future(message = "La fecha de salida debe ser futura")
        LocalDate fechaSalida,

        @NotBlank(message = "El tipo de habitación es obligatorio")
        String tipoHabitacion,

        @NotNull(message = "El número de habitaciones es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 habitación")
        @Max(value = 10, message = "El máximo de habitaciones es 10")
        Integer numeroHabitaciones,

        @NotNull(message = "El número de huéspedes es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 huésped")
        Integer numeroHuespedes,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
        BigDecimal precio,

        String codigoMoneda,

        String observaciones,

        String codigoConfirmacion
) {}