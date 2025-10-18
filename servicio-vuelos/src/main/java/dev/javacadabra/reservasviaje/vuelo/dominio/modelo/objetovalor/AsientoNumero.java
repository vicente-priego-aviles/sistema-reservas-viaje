package dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record AsientoNumero(String valor) {

    public AsientoNumero {
        if (valor == null || !valor.matches("\\d{1,2}[A-F]")) {
            throw new IllegalArgumentException("Formato de asiento inválido. Debe ser como: 12A, 5F");
        }
    }
}
