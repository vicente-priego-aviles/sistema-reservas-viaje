package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida;

import java.io.Serializable;

/**
 * DTO de salida para la dirección postal de un cliente.
 *
 * <p>Contiene todos los componentes de la dirección postal del cliente.
 *
 * <p>Este DTO se usa como parte de {@link ClienteDTO} y en respuestas
 * donde se necesita la dirección completa del cliente.
 *
 * @param calle dirección - calle y número
 * @param ciudad dirección - ciudad
 * @param codigoPostal dirección - código postal
 * @param provincia dirección - provincia
 * @param pais dirección - país
 * @param direccionCompleta dirección formateada completa
 * @param direccionResumida dirección resumida (ciudad, país)
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record DireccionDTO(
        String calle,
        String ciudad,
        String codigoPostal,
        String provincia,
        String pais,
        String direccionCompleta,
        String direccionResumida
) implements Serializable {
}