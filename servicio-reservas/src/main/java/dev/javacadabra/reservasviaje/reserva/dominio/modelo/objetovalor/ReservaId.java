// ============================================
// 🟢 DOMINIO - Value Objects
// ============================================
package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una reserva.
 * Es inmutable y garantiza que siempre contiene un valor válido.
 */
@ValueObject
@Getter
public class ReservaId implements Serializable {

    private final String valor;

    /**
     * Constructor privado para forzar el uso de métodos factory.
     */
    private ReservaId(String valor) {
        if (StringUtils.isBlank(valor)) {
            throw new IllegalArgumentException("El ID de reserva no puede estar vacío");
        }
        this.valor = valor.trim();
    }

    /**
     * Crea un nuevo ReservaId a partir de un String.
     *
     * @param valor el valor del ID
     * @return nueva instancia de ReservaId
     * @throws IllegalArgumentException si el valor es nulo o vacío
     */
    public static ReservaId de(String valor) {
        return new ReservaId(valor);
    }

    /**
     * Crea un nuevo ReservaId a partir de un UUID.
     *
     * @param uuid el UUID
     * @return nueva instancia de ReservaId
     * @throws IllegalArgumentException si el UUID es nulo
     */
    public static ReservaId de(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("El UUID no puede ser nulo");
        }
        return new ReservaId(uuid.toString());
    }

    /**
     * Genera un nuevo ReservaId aleatorio usando UUID.
     *
     * @return nueva instancia de ReservaId con valor aleatorio
     */
    public static ReservaId generar() {
        return new ReservaId(UUID.randomUUID().toString());
    }

    /**
     * Convierte el ReservaId a UUID si es posible.
     *
     * @return UUID correspondiente
     * @throws IllegalArgumentException si el valor no es un UUID válido
     */
    public UUID comoUUID() {
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "El valor '" + valor + "' no es un UUID válido", e
            );
        }
    }
}
