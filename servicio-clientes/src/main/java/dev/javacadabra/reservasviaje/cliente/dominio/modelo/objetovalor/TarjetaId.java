package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador único e inmutable de una Tarjeta de Crédito.
 *
 * <p>Este value object encapsula el identificador de una tarjeta
 * garantizando su inmutabilidad y validez.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
public record TarjetaId(String valor) implements Serializable {

    /**
     * Constructor canónico con validación.
     *
     * @param valor UUID de la tarjeta
     * @throws IllegalArgumentException si el valor es nulo o inválido
     */
    public TarjetaId {
        if (StringUtils.isBlank(valor)) {
            throw new IllegalArgumentException("El ID de la tarjeta no puede estar vacío");
        }

        // Validar que sea un UUID válido
        try {
            UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El ID de la tarjeta debe ser un UUID válido: " + valor, e);
        }
    }

    /**
     * Genera un nuevo TarjetaId con un UUID aleatorio.
     *
     * @return nuevo identificador único de tarjeta
     */
    public static TarjetaId generar() {
        return new TarjetaId(UUID.randomUUID().toString());
    }

    /**
     * Crea un TarjetaId a partir de un String.
     *
     * @param valor UUID en formato String
     * @return TarjetaId creado
     * @throws IllegalArgumentException si el valor es inválido
     */
    public static TarjetaId de(String valor) {
        return new TarjetaId(valor);
    }

    /**
     * Crea un TarjetaId a partir de un UUID.
     *
     * @param uuid UUID de la tarjeta
     * @return TarjetaId creado
     * @throws IllegalArgumentException si el UUID es nulo
     */
    public static TarjetaId de(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("El UUID no puede ser nulo");
        }
        return new TarjetaId(uuid.toString());
    }

    /**
     * Convierte el TarjetaId a UUID.
     *
     * @return UUID de la tarjeta
     */
    public UUID comoUUID() {
        return UUID.fromString(valor);
    }

    @Override
    public String toString() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TarjetaId tarjetaId = (TarjetaId) o;
        return Objects.equals(valor, tarjetaId.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
