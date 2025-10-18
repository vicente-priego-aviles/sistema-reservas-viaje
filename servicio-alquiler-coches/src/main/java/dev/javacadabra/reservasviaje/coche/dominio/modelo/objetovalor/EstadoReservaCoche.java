package dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum EstadoReservaCoche {
    PENDIENTE,
    RESERVADA,
    CANCELADA,
    COMPLETADA
}