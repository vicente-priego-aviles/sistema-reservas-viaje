// ============================================
// 🟢 DOMINIO - Value Objects
// ============================================
package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;
import java.util.UUID;

@ValueObject
public record ReservaId(String valor) {
    public ReservaId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("ReservaId no puede estar vacío");
        }
    }

    public static ReservaId generar() {
        return new ReservaId(UUID.randomUUID().toString());
    }
}

