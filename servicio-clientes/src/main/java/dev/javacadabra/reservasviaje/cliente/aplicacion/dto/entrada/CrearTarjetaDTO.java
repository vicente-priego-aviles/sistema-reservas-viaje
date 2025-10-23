package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * DTO de entrada para crear una tarjeta de crédito.
 *
 * <p>Contiene los datos necesarios para crear una nueva tarjeta de crédito
 * asociada a un cliente. Este DTO se usa tanto en la creación de clientes
 * (tarjeta inicial obligatoria) como al agregar tarjetas adicionales.
 *
 * <p><strong>Validaciones:</strong>
 * <ul>
 *   <li>Número de tarjeta: 13-19 dígitos</li>
 *   <li>Fecha de expiración: formato MM/YY</li>
 *   <li>CVV: 3-4 dígitos</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong> Los datos sensibles (número y CVV) deben
 * ser encriptados en la capa de infraestructura antes de persistirse.
 *
 * @param numeroTarjeta número completo de la tarjeta (13-19 dígitos)
 * @param fechaExpiracion fecha de expiración en formato MM/YY (ej: 12/25)
 * @param cvv código de seguridad CVV (3-4 dígitos)
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record CrearTarjetaDTO(

        @NotBlank(message = "El número de tarjeta es obligatorio")
        @Pattern(
                regexp = "^[0-9]{13,19}$",
                message = "El número de tarjeta debe tener entre 13 y 19 dígitos"
        )
        String numeroTarjeta,

        @NotBlank(message = "La fecha de expiración es obligatoria")
        @Pattern(
                regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$",
                message = "La fecha de expiración debe tener formato MM/YY (ej: 12/25)"
        )
        String fechaExpiracion,

        @NotBlank(message = "El CVV es obligatorio")
        @Pattern(
                regexp = "^[0-9]{3,4}$",
                message = "El CVV debe tener 3 o 4 dígitos"
        )
        String cvv

) implements Serializable {

    /**
     * Obtiene el mes de expiración (MM).
     *
     * @return mes de expiración (01-12)
     */
    public String obtenerMesExpiracion() {
        if (fechaExpiracion == null || !fechaExpiracion.contains("/")) {
            throw new IllegalStateException("Fecha de expiración inválida");
        }
        return fechaExpiracion.split("/")[0];
    }

    /**
     * Obtiene el año de expiración (YY).
     *
     * @return año de expiración (dos últimos dígitos)
     */
    public String obtenerAnioExpiracion() {
        if (fechaExpiracion == null || !fechaExpiracion.contains("/")) {
            throw new IllegalStateException("Fecha de expiración inválida");
        }
        return fechaExpiracion.split("/")[1];
    }

    /**
     * Obtiene el año de expiración completo (YYYY).
     *
     * @return año de expiración completo (ej: 2025)
     */
    public int obtenerAnioExpiracionCompleto() {
        String anioCorto = obtenerAnioExpiracion();
        int anio = Integer.parseInt(anioCorto);

        // Asumimos que si es < 50, es 20XX, sino 19XX
        // (aunque tarjetas con 19XX ya estarían expiradas)
        return anio < 50 ? 2000 + anio : 1900 + anio;
    }
}