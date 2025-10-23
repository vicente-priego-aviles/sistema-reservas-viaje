package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador único e inmutable de un Cliente.
 *
 * <p>Este value object encapsula el identificador del agregado Cliente
 * garantizando su inmutabilidad y validez.
 *
 * <p>Características:
 * <ul>
 *   <li>Inmutable: Una vez creado no puede modificarse</li>
 *   <li>Válido: Siempre contiene un UUID válido</li>
 *   <li>Comparable: Dos ClienteId con el mismo UUID son iguales</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
public record ClienteId(String valor) implements Serializable {

    /**
     * Constructor canónico con validación.
     *
     * @param valor UUID del cliente
     * @throws IllegalArgumentException si el valor es nulo o inválido
     */
    public ClienteId {
        if (StringUtils.isBlank(valor)) {
            throw new IllegalArgumentException("El ID del cliente no puede estar vacío");
        }

        // Validar que sea un UUID válido
        try {
            UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El ID del cliente debe ser un UUID válido: " + valor, e);
        }
    }

    /**
     * Genera un nuevo ClienteId con un UUID aleatorio.
     *
     * @return nuevo identificador único de cliente
     */
    public static ClienteId generar() {
        return new ClienteId(UUID.randomUUID().toString());
    }

    /**
     * Crea un ClienteId a partir de un String.
     *
     * @param valor UUID en formato String
     * @return ClienteId creado
     * @throws IllegalArgumentException si el valor es inválido
     */
    public static ClienteId de(String valor) {
        return new ClienteId(valor);
    }

    /**
     * Crea un ClienteId a partir de un UUID.
     *
     * @param uuid UUID del cliente
     * @return ClienteId creado
     * @throws IllegalArgumentException si el UUID es nulo
     */
    public static ClienteId de(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("El UUID no puede ser nulo");
        }
        return new ClienteId(uuid.toString());
    }

    /**
     * Convierte el ClienteId a UUID.
     *
     * @return UUID del cliente
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
        ClienteId clienteId = (ClienteId) o;
        return Objects.equals(valor, clienteId.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
