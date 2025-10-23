package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * Dirección postal del cliente.
 *
 * <p>Value object inmutable que encapsula la dirección completa
 * de un cliente y garantiza su validez.
 *
 * <p>Validaciones:
 * <ul>
 *   <li>Calle no vacía</li>
 *   <li>Ciudad no vacía</li>
 *   <li>Código postal con formato válido</li>
 *   <li>País no vacío</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
public record Direccion(
        String calle,
        String ciudad,
        String codigoPostal,
        String provincia,
        String pais
) implements Serializable {

    /**
     * Constructor canónico con validaciones.
     *
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public Direccion {
        validarCalle(calle);
        validarCiudad(ciudad);
        validarCodigoPostal(codigoPostal);
        validarProvincia(provincia);
        validarPais(pais);
    }

    private void validarCalle(String calle) {
        if (StringUtils.isBlank(calle)) {
            throw new IllegalArgumentException("La calle no puede estar vacía");
        }
        if (calle.length() < 5) {
            throw new IllegalArgumentException("La calle debe tener al menos 5 caracteres");
        }
        if (calle.length() > 200) {
            throw new IllegalArgumentException("La calle no puede exceder 200 caracteres");
        }
    }

    private void validarCiudad(String ciudad) {
        if (StringUtils.isBlank(ciudad)) {
            throw new IllegalArgumentException("La ciudad no puede estar vacía");
        }
        if (ciudad.length() < 2) {
            throw new IllegalArgumentException("La ciudad debe tener al menos 2 caracteres");
        }
        if (ciudad.length() > 100) {
            throw new IllegalArgumentException("La ciudad no puede exceder 100 caracteres");
        }
    }

    private void validarCodigoPostal(String codigoPostal) {
        if (StringUtils.isBlank(codigoPostal)) {
            throw new IllegalArgumentException("El código postal no puede estar vacío");
        }

        // Validar formato general (números y letras, entre 4 y 10 caracteres)
        String codigoLimpio = codigoPostal.replaceAll("[\\s-]", "");
        if (codigoLimpio.length() < 4 || codigoLimpio.length() > 10) {
            throw new IllegalArgumentException("El código postal debe tener entre 4 y 10 caracteres");
        }

        if (!codigoLimpio.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("El código postal solo puede contener letras mayúsculas y números");
        }
    }

    private void validarProvincia(String provincia) {
        if (StringUtils.isBlank(provincia)) {
            throw new IllegalArgumentException("La provincia no puede estar vacía");
        }
        if (provincia.length() < 2) {
            throw new IllegalArgumentException("La provincia debe tener al menos 2 caracteres");
        }
        if (provincia.length() > 100) {
            throw new IllegalArgumentException("La provincia no puede exceder 100 caracteres");
        }
    }

    private void validarPais(String pais) {
        if (StringUtils.isBlank(pais)) {
            throw new IllegalArgumentException("El país no puede estar vacío");
        }
        if (pais.length() < 2) {
            throw new IllegalArgumentException("El país debe tener al menos 2 caracteres");
        }
        if (pais.length() > 100) {
            throw new IllegalArgumentException("El país no puede exceder 100 caracteres");
        }
    }

    /**
     * Obtiene la dirección completa en formato legible.
     *
     * @return dirección formateada
     */
    public String obtenerDireccionCompleta() {
        return String.format("%s, %s, %s, %s, %s",
                calle, ciudad, codigoPostal, provincia, pais);
    }

    /**
     * Obtiene una representación resumida de la dirección (ciudad, país).
     *
     * @return dirección resumida
     */
    public String obtenerDireccionResumida() {
        return ciudad + ", " + pais;
    }

    /**
     * Crea una copia con una nueva calle.
     *
     * @param nuevaCalle nueva calle
     * @return nueva instancia con la calle actualizada
     */
    public Direccion conCalle(String nuevaCalle) {
        return new Direccion(nuevaCalle, ciudad, codigoPostal, provincia, pais);
    }

    /**
     * Crea una copia con una nueva ciudad.
     *
     * @param nuevaCiudad nueva ciudad
     * @return nueva instancia con la ciudad actualizada
     */
    public Direccion conCiudad(String nuevaCiudad) {
        return new Direccion(calle, nuevaCiudad, codigoPostal, provincia, pais);
    }

    /**
     * Crea una copia con un nuevo código postal.
     *
     * @param nuevoCodigoPostal nuevo código postal
     * @return nueva instancia con el código postal actualizado
     */
    public Direccion conCodigoPostal(String nuevoCodigoPostal) {
        return new Direccion(calle, ciudad, nuevoCodigoPostal, provincia, pais);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Direccion direccion = (Direccion) o;
        return Objects.equals(calle, direccion.calle) &&
               Objects.equals(ciudad, direccion.ciudad) &&
               Objects.equals(codigoPostal, direccion.codigoPostal) &&
               Objects.equals(provincia, direccion.provincia) &&
               Objects.equals(pais, direccion.pais);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calle, ciudad, codigoPostal, provincia, pais);
    }

    @Override
    public String toString() {
        return "Direccion{" +
               "calle='" + calle + '\'' +
               ", ciudad='" + ciudad + '\'' +
               ", codigoPostal='" + codigoPostal + '\'' +
               ", provincia='" + provincia + '\'' +
               ", pais='" + pais + '\'' +
               '}';
    }
}
