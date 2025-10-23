package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Datos personales del cliente.
 *
 * <p>Value object inmutable que encapsula la información personal
 * de un cliente y garantiza su validez.
 *
 * <p>Validaciones:
 * <ul>
 *   <li>Nombre y apellidos no vacíos</li>
 *   <li>Email con formato válido</li>
 *   <li>Teléfono con formato válido (opcional)</li>
 *   <li>Fecha de nacimiento que implique mayor de edad (18 años)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
public record DatosPersonales(
        String nombre,
        String apellidos,
        String email,
        String telefono,
        LocalDate fechaNacimiento
) implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
            "^\\+?[0-9]{9,15}$"
    );

    private static final int EDAD_MINIMA = 18;

    /**
     * Constructor canónico con validaciones.
     *
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public DatosPersonales {
        validarNombre(nombre);
        validarApellidos(apellidos);
        validarEmail(email);
        validarTelefono(telefono);
        validarFechaNacimiento(fechaNacimiento);
    }

    private void validarNombre(String nombre) {
        if (StringUtils.isBlank(nombre)) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (nombre.length() < 2) {
            throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }
    }

    private void validarApellidos(String apellidos) {
        if (StringUtils.isBlank(apellidos)) {
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos");
        }
        if (apellidos.length() < 2) {
            throw new IllegalArgumentException("Los apellidos deben tener al menos 2 caracteres");
        }
        if (apellidos.length() > 100) {
            throw new IllegalArgumentException("Los apellidos no pueden exceder 100 caracteres");
        }
    }

    private void validarEmail(String email) {
        if (StringUtils.isBlank(email)) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato válido: " + email);
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("El email no puede exceder 255 caracteres");
        }
    }

    private void validarTelefono(String telefono) {
        // El teléfono es opcional
        if (StringUtils.isNotBlank(telefono)) {
            if (!TELEFONO_PATTERN.matcher(telefono).matches()) {
                throw new IllegalArgumentException("El teléfono no tiene un formato válido: " + telefono);
            }
        }
    }

    private void validarFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula");
        }

        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }

        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < EDAD_MINIMA) {
            throw new IllegalArgumentException(
                    String.format("El cliente debe ser mayor de %d años. Edad actual: %d", EDAD_MINIMA, edad)
            );
        }

        // Validar que no sea excesivamente antigua (más de 120 años)
        if (edad > 120) {
            throw new IllegalArgumentException("La fecha de nacimiento no es realista (más de 120 años)");
        }
    }

    /**
     * Calcula la edad actual del cliente.
     *
     * @return edad en años
     */
    public int calcularEdad() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    /**
     * Obtiene el nombre completo (nombre + apellidos).
     *
     * @return nombre completo del cliente
     */
    public String obtenerNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /**
     * Verifica si el cliente es mayor de edad.
     *
     * @return true si es mayor de edad, false en caso contrario
     */
    public boolean esMayorDeEdad() {
        return calcularEdad() >= EDAD_MINIMA;
    }

    /**
     * Crea una copia con un nuevo email.
     *
     * @param nuevoEmail nuevo email del cliente
     * @return nueva instancia con el email actualizado
     */
    public DatosPersonales conEmail(String nuevoEmail) {
        return new DatosPersonales(nombre, apellidos, nuevoEmail, telefono, fechaNacimiento);
    }

    /**
     * Crea una copia con un nuevo teléfono.
     *
     * @param nuevoTelefono nuevo teléfono del cliente
     * @return nueva instancia con el teléfono actualizado
     */
    public DatosPersonales conTelefono(String nuevoTelefono) {
        return new DatosPersonales(nombre, apellidos, email, nuevoTelefono, fechaNacimiento);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatosPersonales that = (DatosPersonales) o;
        return Objects.equals(nombre, that.nombre) &&
               Objects.equals(apellidos, that.apellidos) &&
               Objects.equals(email, that.email) &&
               Objects.equals(telefono, that.telefono) &&
               Objects.equals(fechaNacimiento, that.fechaNacimiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellidos, email, telefono, fechaNacimiento);
    }

    @Override
    public String toString() {
        return "DatosPersonales{" +
               "nombre='" + nombre + '\'' +
               ", apellidos='" + apellidos + '\'' +
               ", email='" + email + '\'' +
               ", telefono='" + (telefono != null ? "***" : "N/A") + '\'' +
               ", edad=" + calcularEdad() +
               '}';
    }
}

