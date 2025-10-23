package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida;

import java.io.Serializable;
import java.time.YearMonth;

/**
 * DTO de salida para una tarjeta de crédito.
 *
 * <p>Contiene los datos de una tarjeta de crédito con información sensible
 * enmascarada por motivos de seguridad (PCI DSS compliance).
 *
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>El número de tarjeta se devuelve enmascarado (ej: **** **** **** 1234)</li>
 *   <li>El CVV NUNCA se devuelve en respuestas (no se persiste en BD)</li>
 *   <li>Solo se muestran los últimos 4 dígitos para identificación</li>
 * </ul>
 *
 * @param tarjetaId identificador único de la tarjeta
 * @param numeroEnmascarado número de tarjeta enmascarado (ej: **** **** **** 1234)
 * @param ultimosDigitos últimos 4 dígitos de la tarjeta
 * @param fechaExpiracion fecha de expiración (año-mes)
 * @param tipoTarjeta tipo de tarjeta (VISA, MASTERCARD, AMEX, etc.)
 * @param nombreTipoTarjeta nombre descriptivo del tipo de tarjeta
 * @param validada indica si la tarjeta fue validada externamente
 * @param esValida indica si la tarjeta es válida para uso (no expirada + validada)
 * @param estaExpirada indica si la tarjeta está expirada
 * @param expiraPronto indica si expira en los próximos 3 meses
 * @param mesesHastaExpiracion meses hasta la expiración (negativo si ya expiró)
 * @param motivoRechazo motivo de rechazo si la tarjeta es inválida (puede ser null)
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record TarjetaCreditoDTO(
        String tarjetaId,
        String numeroEnmascarado,
        String ultimosDigitos,
        YearMonth fechaExpiracion,
        String tipoTarjeta,
        String nombreTipoTarjeta,
        boolean validada,
        boolean esValida,
        boolean estaExpirada,
        boolean expiraPronto,
        long mesesHastaExpiracion,
        String motivoRechazo
) implements Serializable {
}