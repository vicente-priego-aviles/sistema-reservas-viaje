package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

/**
 * Tipos de tarjetas de crédito aceptadas en el sistema.
 *
 * <p>Cada tipo de tarjeta tiene asociado:
 * <ul>
 *   <li>Un patrón regex para validar el formato del número</li>
 *   <li>Una longitud esperada del número</li>
 *   <li>Una longitud esperada del CVV</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
@Getter
public enum TipoTarjeta {

    /**
     * Visa - Números que empiezan con 4
     * Longitud: 13, 16 o 19 dígitos
     * CVV: 3 dígitos
     */
    VISA(
            "Visa",
            "^4[0-9]{12}(?:[0-9]{3})?(?:[0-9]{3})?$",
            new int[]{13, 16, 19},
            3
    ),

    /**
     * Mastercard - Números que empiezan con 51-55 o 2221-2720
     * Longitud: 16 dígitos
     * CVV: 3 dígitos
     */
    MASTERCARD(
            "Mastercard",
            "^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$",
            new int[]{16},
            3
    ),

    /**
     * American Express - Números que empiezan con 34 o 37
     * Longitud: 15 dígitos
     * CVV: 4 dígitos
     */
    AMERICAN_EXPRESS(
            "American Express",
            "^3[47][0-9]{13}$",
            new int[]{15},
            4
    ),

    /**
     * Discover - Números que empiezan con 6011, 622126-622925, 644-649, o 65
     * Longitud: 16 dígitos
     * CVV: 3 dígitos
     */
    DISCOVER(
            "Discover",
            "^6(?:011|5[0-9]{2}|4[4-9][0-9]|22(?:1(?:2[6-9]|[3-9][0-9])|[2-8][0-9]{2}|9(?:[01][0-9]|2[0-5])))[0-9]{12}$",
            new int[]{16},
            3
    );

    private final String nombre;
    private final Pattern patron;
    private final int[] longitudesValidas;
    private final int longitudCVV;

    TipoTarjeta(String nombre, String regex, int[] longitudesValidas, int longitudCVV) {
        this.nombre = nombre;
        this.patron = Pattern.compile(regex);
        this.longitudesValidas = longitudesValidas;
        this.longitudCVV = longitudCVV;
    }

    /**
     * Valida si un número de tarjeta coincide con el patrón de este tipo.
     *
     * @param numeroTarjeta número de tarjeta a validar (solo dígitos)
     * @return true si el número es válido para este tipo, false en caso contrario
     */
    public boolean validarNumero(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) {
            return false;
        }

        // Validar longitud
        boolean longitudValida = false;
        for (int longitud : longitudesValidas) {
            if (numeroTarjeta.length() == longitud) {
                longitudValida = true;
                break;
            }
        }

        if (!longitudValida) {
            return false;
        }

        // Validar patrón
        return patron.matcher(numeroTarjeta).matches();
    }

    /**
     * Detecta el tipo de tarjeta a partir de su número.
     *
     * @param numeroTarjeta número de tarjeta (solo dígitos)
     * @return tipo de tarjeta detectado, o null si no se reconoce
     */
    public static TipoTarjeta detectarTipo(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) {
            return null;
        }

        for (TipoTarjeta tipo : values()) {
            if (tipo.validarNumero(numeroTarjeta)) {
                return tipo;
            }
        }

        return null;
    }

    /**
     * Valida si un CVV tiene la longitud correcta para este tipo de tarjeta.
     *
     * @param cvv código CVV a validar
     * @return true si la longitud es correcta, false en caso contrario
     */
    public boolean validarLongitudCVV(String cvv) {
        return cvv != null && cvv.length() == longitudCVV;
    }
}
