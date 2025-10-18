package dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;
import java.util.UUID;

@ValueObject
public record ReservaVueloId(String valor) {
    public ReservaVueloId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("ReservaVueloId no puede estar vacío");
        }
    }

    public static ReservaVueloId generar() {
        return new ReservaVueloId(UUID.randomUUID().toString());
    }
}

