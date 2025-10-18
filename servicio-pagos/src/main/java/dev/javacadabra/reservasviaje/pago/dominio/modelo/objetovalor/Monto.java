package dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Monto(Double valor) {
    public Monto {
        if (valor == null || valor < 0) {
            throw new IllegalArgumentException("Monto no puede ser negativo");
        }
    }
}
