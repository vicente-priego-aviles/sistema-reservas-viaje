package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida con la información de una reserva de hotel creada.
 */
public record ReservaHotelRespuestaDTO(
        String reservaId,
        String nombreHotel,
        String ciudad,
        String direccion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String tipoHabitacion,
        Integer numeroHabitaciones,
        Integer numeroHuespedes,
        Long numeroNoches,
        BigDecimal precio,
        String codigoMoneda,
        String estado,
        String clienteId,
        String codigoConfirmacion,
        LocalDateTime fechaCreacion
) {}