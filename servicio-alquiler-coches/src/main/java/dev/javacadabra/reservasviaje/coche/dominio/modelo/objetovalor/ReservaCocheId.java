package dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;
import java.util.UUID;

@ValueObject
public record ReservaCocheId(String valor) {
    public static ReservaCocheId generar() {
        return new ReservaCocheId(UUID.randomUUID().toString());
    }
}