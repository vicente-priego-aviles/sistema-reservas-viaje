package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO de entrada para actualizar los datos personales de un cliente.
 *
 * <p>Permite actualizar nombre, apellidos, email, teléfono y fecha de nacimiento.
 * El DNI NO se puede actualizar por motivos de seguridad y compliance.
 *
 * <p><strong>Validaciones:</strong>
 * <ul>
 *   <li>Nombre y apellidos: mínimo 2 caracteres</li>
 *   <li>Email: formato válido y único en el sistema</li>
 *   <li>Teléfono: formato internacional (opcional)</li>
 *   <li>Fecha nacimiento: mayor de 18 años</li>
 * </ul>
 *
 * <p><strong>Reglas de negocio:</strong>
 * <ul>
 *   <li>Solo clientes ACTIVOS o EN_PROCESO_RESERVA pueden actualizar datos</li>
 *   <li>Clientes BLOQUEADOS o INACTIVOS no pueden actualizar</li>
 *   <li>Si se cambia el email, puede requerir revalidación</li>
 * </ul>
 *
 * @param nombre nuevo nombre del cliente
 * @param apellidos nuevos apellidos del cliente
 * @param email nuevo email del cliente (debe ser único)
 * @param telefono nuevo teléfono del cliente (opcional)
 * @param fechaNacimiento nueva fecha de nacimiento (debe ser mayor de 18 años)
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record ActualizarDatosPersonalesDTO(

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
        LocalDate fechaNacimiento

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
     * Normaliza el email a minúsculas.
     *
     * @return email normalizado en minúsculas
     */
    public String obtenerEmailNormalizado() {
        return email.trim().toLowerCase();
    }
}