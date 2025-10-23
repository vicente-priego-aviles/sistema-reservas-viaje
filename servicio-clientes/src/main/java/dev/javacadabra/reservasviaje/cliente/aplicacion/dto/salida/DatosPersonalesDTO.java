package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO de salida para los datos personales de un cliente.
 *
 * <p>Contiene la información personal del cliente incluyendo DNI enmascarado
 * para protección de datos personales (RGPD).
 *
 * <p>Este DTO se usa como parte de {@link ClienteDTO} y en respuestas
 * donde se necesitan los datos personales del cliente.
 *
 * @param dniEnmascarado DNI enmascarado del cliente (ej: "123****78Z")
 * @param nombre nombre del cliente
 * @param apellidos apellidos del cliente
 * @param nombreCompleto nombre completo (nombre + apellidos)
 * @param email email del cliente
 * @param telefono teléfono del cliente (puede ser null)
 * @param fechaNacimiento fecha de nacimiento
 * @param edad edad actual calculada en años
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record DatosPersonalesDTO(
        String dniEnmascarado,
        String nombre,
        String apellidos,
        String nombreCompleto,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        int edad
) implements Serializable {
}