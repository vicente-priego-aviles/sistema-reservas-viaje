package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record DetalleCoche(
        String modelo,
        String matricula,
        String puntoRecogida,
        String numeroReserva
) {}
