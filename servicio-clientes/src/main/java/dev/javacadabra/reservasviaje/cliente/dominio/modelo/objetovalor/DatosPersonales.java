package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Datos personales del cliente.
 *
 * <p>Value object inmutable que encapsula la información personal
 * de un cliente y garantiza su validez.
 *
 * <p>Validaciones:
 * <ul>
 *   <li>DNI con formato válido (8 dígitos + letra de control correcta)</li>
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
@Getter
@EqualsAndHashCode
@ToString(exclude = {"dni", "telefono"})
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatosPersonales implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
            "^\\+?[0-9]{9,15}$"
    );

    private static final Pattern DNI_PATTERN = Pattern.compile(
            "^\\d{8}[A-Z]$"
    );

    private static final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int EDAD_MINIMA = 18;

    String dni;
    String nombre;
    String apellidos;
    String email;
    String telefono;
    LocalDate fechaNacimiento;

    /**
     * Clase Builder personalizada con validaciones.
     * Lombok genera automáticamente el builder base, pero lo personalizamos
     * para agregar validaciones en el método build().
     */
    public static class DatosPersonalesBuilder {

        /**
         * Construye y valida la instancia de DatosPersonales.
         *
         * @return instancia validada de DatosPersonales
         * @throws IllegalArgumentException si algún dato es inválido
         */
        public DatosPersonales build() {
            // Validar antes de construir
            validarDni(this.dni);
            validarNombre(this.nombre);
            validarApellidos(this.apellidos);
            validarEmail(this.email);
            validarTelefono(this.telefono);
            validarFechaNacimiento(this.fechaNacimiento);

            // Construir usando el método interno generado por Lombok
            return new DatosPersonales(
                    this.dni,
                    this.nombre,
                    this.apellidos,
                    this.email,
                    this.telefono,
                    this.fechaNacimiento
            );
        }

        private void validarDni(String dni) {
            if (StringUtils.isBlank(dni)) {
                throw new IllegalArgumentException("El DNI no puede estar vacío");
            }

            String dniNormalizado = dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");

            if (!DNI_PATTERN.matcher(dniNormalizado).matches()) {
                throw new IllegalArgumentException(
                        "El DNI debe tener 8 dígitos seguidos de una letra (ej: 12345678Z)"
                );
            }

            // Validar letra de control del DNI
            int numerosDNI = Integer.parseInt(dniNormalizado.substring(0, 8));
            char letraEsperada = LETRAS_DNI.charAt(numerosDNI % 23);
            char letraProporcionada = dniNormalizado.charAt(8);

            if (letraProporcionada != letraEsperada) {
                throw new IllegalArgumentException(
                        String.format("La letra del DNI no es correcta. Debería ser %s", letraEsperada)
                );
            }
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
    }

    public DatosPersonales(
            String dni,
            String nombre,
            String apellidos,
            String email,
            String telefono,
            LocalDate fechaNacimiento) {

        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
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
     * Obtiene el DNI enmascarado para mostrar en logs
     * o interfaces de usuario (protección RGPD).
     *
     * @return DNI enmascarado (ej: "123****78Z")
     */
    public String obtenerDniEnmascarado() {
        if (dni == null || dni.length() < 4) {
            return "****";
        }
        String dniNormalizado = dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (dniNormalizado.length() < 4) {
            return "****";
        }
        String inicio = dniNormalizado.substring(0, 3);
        String fin = dniNormalizado.substring(dniNormalizado.length() - 2);
        return inicio + "****" + fin;
    }

    /**
     * Crea una copia con un nuevo email.
     *
     * @param nuevoEmail nuevo email del cliente
     * @return nueva instancia con el email actualizado
     */
    public DatosPersonales conEmail(String nuevoEmail) {
        return this.toBuilder()
                .email(nuevoEmail)
                .build();
    }

    /**
     * Crea una copia con un nuevo teléfono.
     *
     * @param nuevoTelefono nuevo teléfono del cliente
     * @return nueva instancia con el teléfono actualizado
     */
    public DatosPersonales conTelefono(String nuevoTelefono) {
        return this.toBuilder()
                .telefono(nuevoTelefono)
                .build();
    }

    /**
     * Crea una copia actualizando los datos de contacto.
     * El DNI y la fecha de nacimiento permanecen inmutables.
     *
     * <p>Este método es ideal para operaciones de actualización de perfil
     * donde ciertos datos identificativos no deben cambiar.
     *
     * @param nuevoNombre nuevo nombre del cliente
     * @param nuevosApellidos nuevos apellidos del cliente
     * @param nuevoEmail nuevo email del cliente
     * @param nuevoTelefono nuevo teléfono del cliente (puede ser null)
     * @return nueva instancia con los datos de contacto actualizados
     * @throws IllegalArgumentException si los nuevos datos no son válidos
     */
    public DatosPersonales actualizarDatosContacto(
            String nuevoNombre,
            String nuevosApellidos,
            String nuevoEmail,
            String nuevoTelefono) {

        return this.toBuilder()
                .nombre(nuevoNombre)
                .apellidos(nuevosApellidos)
                .email(nuevoEmail)
                .telefono(nuevoTelefono)
                .build();
    }

    /**
     * Representación personalizada del objeto para logs.
     * Enmascara datos sensibles (DNI y teléfono).
     *
     * @return representación del objeto con datos sensibles enmascarados
     */
    public String toStringSeguro() {
        return "DatosPersonales{" +
               "dni='" + obtenerDniEnmascarado() + '\'' +
               ", nombre='" + nombre + '\'' +
               ", apellidos='" + apellidos + '\'' +
               ", email='" + email + '\'' +
               ", telefono='" + (telefono != null ? "***" : "N/A") + '\'' +
               ", edad=" + calcularEdad() +
               '}';
    }
}