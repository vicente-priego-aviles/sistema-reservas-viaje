package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO de entrada para crear un nuevo cliente en el sistema.
 *
 * <p>Contiene todos los datos necesarios para crear un cliente completo:
 * datos personales (incluyendo DNI), dirección postal y tarjeta inicial.
 *
 * <p><strong>Validaciones:</strong>
 * <ul>
 *   <li>DNI: formato español válido (8 dígitos + letra)</li>
 *   <li>Nombre y apellidos: mínimo 2 caracteres</li>
 *   <li>Email: formato válido y único en el sistema</li>
 *   <li>Teléfono: formato internacional (opcional)</li>
 *   <li>Fecha nacimiento: mayor de 18 años</li>
 *   <li>Dirección: todos los campos obligatorios</li>
 *   <li>Tarjeta: datos válidos (número, fecha, CVV)</li>
 * </ul>
 *
 * <p><strong>Reglas de negocio:</strong>
 * <ul>
 *   <li>El cliente se crea con estado PENDIENTE_VALIDACION</li>
 *   <li>Se requiere al menos una tarjeta inicial</li>
 *   <li>El email debe ser único en el sistema</li>
 *   <li>El DNI debe ser único en el sistema</li>
 * </ul>
 *
 * @param dni documento nacional de identidad (formato: 12345678Z)
 * @param nombre nombre del cliente
 * @param apellidos apellidos del cliente
 * @param email email del cliente (único)
 * @param telefono teléfono del cliente (opcional, formato internacional)
 * @param fechaNacimiento fecha de nacimiento (debe ser mayor de 18 años)
 * @param calle dirección - calle y número
 * @param ciudad dirección - ciudad
 * @param codigoPostal dirección - código postal
 * @param provincia dirección - provincia
 * @param pais dirección - país
 * @param tarjeta tarjeta de crédito inicial (obligatoria)
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record CrearClienteDTO(

        // ==================== DATOS PERSONALES ====================

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(
                regexp = "^[0-9]{8}[A-Z]$",
                message = "El DNI debe tener 8 dígitos seguidos de una letra mayúscula (ej: 12345678Z)"
        )
        String dni,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
        String apellidos,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 255, message = "El email no puede exceder 255 caracteres")
        String email,

        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "El teléfono debe tener formato internacional (9-15 dígitos, puede empezar con +)"
        )
        String telefono, // Opcional

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe estar en el pasado")
        LocalDate fechaNacimiento,

        // ==================== DIRECCIÓN ====================

        @NotBlank(message = "La calle es obligatoria")
        @Size(min = 5, max = 200, message = "La calle debe tener entre 5 y 200 caracteres")
        String calle,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(min = 2, max = 100, message = "La ciudad debe tener entre 2 y 100 caracteres")
        String ciudad,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(
                regexp = "^[A-Z0-9]{4,10}$",
                message = "El código postal debe tener entre 4 y 10 caracteres alfanuméricos"
        )
        String codigoPostal,

        @NotBlank(message = "La provincia es obligatoria")
        @Size(min = 2, max = 100, message = "La provincia debe tener entre 2 y 100 caracteres")
        String provincia,

        @NotBlank(message = "El país es obligatorio")
        @Size(min = 2, max = 100, message = "El país debe tener entre 2 y 100 caracteres")
        String pais,

        // ==================== TARJETA INICIAL ====================

        @NotNull(message = "Se requiere una tarjeta de crédito para crear el cliente")
        @Valid
        CrearTarjetaDTO tarjeta

) implements Serializable {

    /**
     * Obtiene el nombre completo del cliente.
     *
     * @return nombre completo (nombre + apellidos)
     */
    public String obtenerNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /**
     * Normaliza el DNI eliminando espacios y guiones.
     *
     * @return DNI normalizado en mayúsculas
     */
    public String obtenerDniNormalizado() {
        return dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    /**
     * Normaliza el email a minúsculas.
     *
     * @return email normalizado en minúsculas
     */
    public String obtenerEmailNormalizado() {
        return email.trim().toLowerCase();
    }

    /**
     * Normaliza el código postal eliminando espacios y guiones.
     *
     * @return código postal normalizado en mayúsculas
     */
    public String obtenerCodigoPostalNormalizado() {
        return codigoPostal.trim().toUpperCase().replaceAll("[\\s-]", "");
    }
}