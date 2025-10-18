package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record DetalleHotel(
        String nombreHotel,
        String numeroHabitacion,
        String tipoHabitacion,
        String numeroReserva
) {}
