package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO de entrada para actualización de datos personales.
 *
 * <p>No incluye DNI ni fecha de nacimiento porque son campos inmutables.
 * Solo permite actualizar datos de contacto.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Builder
@Schema(description = "Datos para actualizar información personal del cliente")
public record ActualizarDatosPersonalesDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @Schema(description = "Nombre del cliente", example = "Juan", required = true)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
        @Schema(description = "Apellidos del cliente", example = "García Pérez", required = true)
        String apellidos,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255, message = "El email no puede exceder 255 caracteres")
        @Schema(description = "Email del cliente", example = "juan.garcia@email.com", required = true)
        String email,

        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "El teléfono debe tener entre 9 y 15 dígitos, opcionalmente con el prefijo +"
        )
        @Schema(description = "Teléfono del cliente", example = "+34612345678")
        String telefono
) {
}