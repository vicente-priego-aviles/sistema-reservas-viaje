package dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;
import java.util.UUID;

@ValueObject
public record ReservaHotelId(String valor) {
    public static ReservaHotelId generar() {
        return new ReservaHotelId(UUID.randomUUID().toString());
    }
}