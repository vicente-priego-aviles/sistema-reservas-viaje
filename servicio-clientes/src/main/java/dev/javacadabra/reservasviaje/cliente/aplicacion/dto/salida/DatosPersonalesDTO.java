package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO de salida con datos personales del cliente.
 *
 * <p>Incluye información calculada como edad y nombre completo.
 * El DNI se devuelve enmascarado para protección de datos (RGPD).
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Builder
@Schema(description = "Datos personales del cliente")
public record DatosPersonalesDTO(

        @Schema(description = "DNI enmascarado del cliente", example = "123****78Z")
        String dniEnmascarado,

        @Schema(description = "Nombre del cliente", example = "Juan")
        String nombre,

        @Schema(description = "Apellidos del cliente", example = "García Pérez")
        String apellidos,

        @Schema(description = "Nombre completo del cliente", example = "Juan García Pérez")
        String nombreCompleto,

        @Schema(description = "Email del cliente", example = "juan.garcia@email.com")
        String email,

        @Schema(description = "Teléfono del cliente", example = "+34612345678")
        String telefono,

        @Schema(description = "Fecha de nacimiento", example = "1990-05-15")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento,

        @Schema(description = "Edad actual del cliente", example = "33")
        int edad
) {
}