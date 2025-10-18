package dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record HabitacionNumero(String valor) {
    public HabitacionNumero {
        if (valor == null || !valor.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Número de habitación inválido");
        }
    }
}
