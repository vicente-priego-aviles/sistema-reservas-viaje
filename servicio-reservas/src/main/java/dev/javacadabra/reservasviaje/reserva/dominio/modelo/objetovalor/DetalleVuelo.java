package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record DetalleVuelo(
        String numeroVuelo,
        String aerolinea,
        String asiento,
        String numeroReserva
) {}
