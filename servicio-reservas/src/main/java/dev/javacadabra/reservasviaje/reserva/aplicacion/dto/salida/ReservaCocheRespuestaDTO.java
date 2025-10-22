package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida con la información de una reserva de coche creada.
 */
public record ReservaCocheRespuestaDTO(
        String reservaId,
        String empresaAlquiler,
        String modeloCoche,
        String categoriaCoche,
        String ubicacionRecogida,
        String ubicacionDevolucion,
        LocalDateTime fechaRecogida,
        LocalDateTime fechaDevolucion,
        Long diasAlquiler,
        BigDecimal precio,
        String codigoMoneda,
        String estado,
        String clienteId,
        String codigoConfirmacion,
        LocalDateTime fechaCreacion
) {}