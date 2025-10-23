package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que representa los tipos de tarjetas de crédito soportados.
 *
 * <p>Cada tipo de tarjeta tiene características específicas:
 * <ul>
 *   <li>Longitud del CVV (3 o 4 dígitos)</li>
 *   <li>Nombre comercial de la tarjeta</li>
 *   <li>Rango de números que identifica el tipo</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum TipoTarjeta {

    /**
     * Visa - CVV de 3 dígitos.
     * Números que empiezan por 4.
     */
    VISA("Visa", 3),

    /**
     * Mastercard - CVV de 3 dígitos.
     * Números 51-55 o 2221-2720.
     */
    MASTERCARD("Mastercard", 3),

    /**
     * American Express - CVV de 4 dígitos.
     * Números que empiezan por 34 o 37.
     */
    AMERICAN_EXPRESS("American Express", 4),

    /**
     * Discover - CVV de 3 dígitos.
     * Números que empiezan por 6011, 622126-622925, 644-649, 65.
     */
    DISCOVER("Discover", 3),

    /**
     * Diners Club - CVV de 3 dígitos.
     * Números que empiezan por 300-305, 36, 38.
     */
    DINERS_CLUB("Diners Club", 3),

    /**
     * JCB - CVV de 3 dígitos.
     * Números que empiezan por 3528-3589.
     */
    JCB("JCB", 3),

    /**
     * Otras tarjetas no identificadas - CVV de 3 dígitos por defecto.
     */
    OTRA("Otra", 3);

    /**
     * Nombre comercial de la tarjeta.
     */
    private final String nombre;

    /**
     * Longitud del CVV (3 o 4 dígitos).
     */
    private final int longitudCVV;

    /**
     * Detecta el tipo de tarjeta según los primeros dígitos.
     *
     * @param primerosDigitos primeros 4-6 dígitos de la tarjeta
     * @return tipo de tarjeta detectado o OTRA si no se reconoce
     */
    public static TipoTarjeta detectarDesdeNumero(String primerosDigitos) {
        if (primerosDigitos == null || primerosDigitos.length() < 2) {
            return OTRA;
        }

        // Visa: empieza con 4
        if (primerosDigitos.startsWith("4")) {
            return VISA;
        }

        // Mastercard: 51-55 o 2221-2720
        if (primerosDigitos.startsWith("5")) {
            try {
                int prefijo = Integer.parseInt(primerosDigitos.substring(0, 2));
                if (prefijo >= 51 && prefijo <= 55) {
                    return MASTERCARD;
                }
            } catch (NumberFormatException e) {
                // Ignorar
            }
        }

        if (primerosDigitos.length() >= 4) {
            try {
                int prefijo = Integer.parseInt(primerosDigitos.substring(0, 4));
                if (prefijo >= 2221 && prefijo <= 2720) {
                    return MASTERCARD;
                }
                // JCB: 3528-3589
                if (prefijo >= 3528 && prefijo <= 3589) {
                    return JCB;
                }
            } catch (NumberFormatException e) {
                // Ignorar
            }
        }

        // American Express: 34 o 37
        if (primerosDigitos.startsWith("34") || primerosDigitos.startsWith("37")) {
            return AMERICAN_EXPRESS;
        }

        // Diners Club: 300-305, 36, 38
        if (primerosDigitos.startsWith("36") || primerosDigitos.startsWith("38")) {
            return DINERS_CLUB;
        }

        if (primerosDigitos.length() >= 3) {
            try {
                int prefijo = Integer.parseInt(primerosDigitos.substring(0, 3));
                if (prefijo >= 300 && prefijo <= 305) {
                    return DINERS_CLUB;
                }
            } catch (NumberFormatException e) {
                // Ignorar
            }
        }

        // Discover: 6011, 622126-622925, 644-649, 65
        if (primerosDigitos.startsWith("6011") || primerosDigitos.startsWith("65")) {
            return DISCOVER;
        }

        if (primerosDigitos.length() >= 3) {
            try {
                int prefijo = Integer.parseInt(primerosDigitos.substring(0, 3));
                if (prefijo >= 644 && prefijo <= 649) {
                    return DISCOVER;
                }
            } catch (NumberFormatException e) {
                // Ignorar
            }
        }

        if (primerosDigitos.length() >= 6) {
            try {
                int prefijo = Integer.parseInt(primerosDigitos.substring(0, 6));
                if (prefijo >= 622126 && prefijo <= 622925) {
                    return DISCOVER;
                }
            } catch (NumberFormatException e) {
                // Ignorar
            }
        }

        // No se reconoce
        return OTRA;
    }

    /**
     * Verifica si este tipo requiere CVV de 4 dígitos.
     *
     * @return true si requiere 4 dígitos (solo AMEX)
     */
    public boolean requiereCVVLargo() {
        return this == AMERICAN_EXPRESS;
    }

    @Override
    public String toString() {
        return nombre;
    }
}