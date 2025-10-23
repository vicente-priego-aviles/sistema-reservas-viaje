package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO de entrada para actualizar la dirección postal de un cliente.
 *
 * <p>Permite actualizar todos los componentes de la dirección postal:
 * calle, ciudad, código postal, provincia y país.
 *
 * <p><strong>Validaciones:</strong>
 * <ul>
 *   <li>Calle: mínimo 5 caracteres</li>
 *   <li>Ciudad: mínimo 2 caracteres</li>
 *   <li>Código postal: formato alfanumérico (4-10 caracteres)</li>
 *   <li>Provincia: mínimo 2 caracteres</li>
 *   <li>País: mínimo 2 caracteres</li>
 * </ul>
 *
 * <p><strong>Reglas de negocio:</strong>
 * <ul>
 *   <li>Solo clientes ACTIVOS o EN_PROCESO_RESERVA pueden actualizar</li>
 *   <li>Clientes BLOQUEADOS o INACTIVOS no pueden actualizar</li>
 *   <li>El cambio de dirección puede afectar servicios disponibles por zona</li>
 * </ul>
 *
 * @param calle dirección - calle y número
 * @param ciudad dirección - ciudad
 * @param codigoPostal dirección - código postal
 * @param provincia dirección - provincia
 * @param pais dirección - país
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record ActualizarDireccionDTO(

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
        String pais

) implements Serializable {

    /**
     * Normaliza el código postal eliminando espacios y guiones.
     *
     * @return código postal normalizado en mayúsculas
     */
    public String obtenerCodigoPostalNormalizado() {
        return codigoPostal.trim().toUpperCase().replaceAll("[\\s-]", "");
    }

    /**
     * Obtiene la dirección en formato resumido (ciudad, país).
     *
     * @return dirección resumida
     */
    public String obtenerDireccionResumida() {
        return ciudad + ", " + pais;
    }
}