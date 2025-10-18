package dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;
import java.util.UUID;

@ValueObject
public record PagoId(String valor) {
    public PagoId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("PagoId no puede estar vacío");
        }
    }

    public static PagoId generar() {
        return new PagoId(UUID.randomUUID().toString());
    }
}