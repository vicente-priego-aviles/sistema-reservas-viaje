package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;

/**
 * Número de tarjeta de crédito encriptado.
 *
 * <p>Value object inmutable que encapsula el número de una tarjeta
 * de crédito, asegurando:
 * <ul>
 *   <li>Validación del formato mediante el algoritmo de Luhn</li>
 *   <li>Encriptación del número (placeholder con Base64)</li>
 *   <li>Enmascaramiento para logs seguros</li>
 *   <li>Detección automática del tipo de tarjeta</li>
 * </ul>
 *
 * <p><strong>NOTA:</strong> La encriptación actual es un placeholder (Base64).
 * En producción debe reemplazarse con un algoritmo robusto (AES-256, RSA, etc.)
 * y gestión segura de claves.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
public record NumeroTarjeta(String valorEncriptado) implements Serializable {

    /**
     * Constructor canónico con validación.
     *
     * @param valorEncriptado número de tarjeta encriptado
     * @throws IllegalArgumentException si el valor es inválido
     */
    public NumeroTarjeta {
        if (StringUtils.isBlank(valorEncriptado)) {
            throw new IllegalArgumentException("El número de tarjeta encriptado no puede estar vacío");
        }
    }

    /**
     * Crea un NumeroTarjeta a partir del número en claro.
     * Valida el formato y lo encripta antes de almacenarlo.
     *
     * @param numeroClaro número de tarjeta en claro (solo dígitos)
     * @return NumeroTarjeta con el valor encriptado
     * @throws IllegalArgumentException si el número es inválido
     */
    public static NumeroTarjeta crear(String numeroClaro) {
        validarFormato(numeroClaro);

        if (!validarLuhn(numeroClaro)) {
            throw new IllegalArgumentException("El número de tarjeta no pasa la validación del algoritmo de Luhn");
        }

        String encriptado = encriptar(numeroClaro);
        return new NumeroTarjeta(encriptado);
    }

    /**
     * Crea un NumeroTarjeta desde un valor ya encriptado.
     * Útil al recuperar datos de la base de datos.
     *
     * @param valorEncriptado número encriptado
     * @return NumeroTarjeta
     */
    public static NumeroTarjeta desdeEncriptado(String valorEncriptado) {
        return new NumeroTarjeta(valorEncriptado);
    }

    /**
     * Desencripta y obtiene el número de tarjeta en claro.
     *
     * <p><strong>ADVERTENCIA:</strong> Usar con precaución. Solo desencriptar
     * cuando sea absolutamente necesario y nunca loguear el valor.
     *
     * @return número de tarjeta desencriptado
     */
    public String desencriptar() {
        return desencriptarPlaceholder(valorEncriptado);
    }

    /**
     * Obtiene el número enmascarado para mostrar al usuario.
     * Formato: **** **** **** 1234
     *
     * @return número enmascarado
     */
    public String obtenerNumeroEnmascarado() {
        String numeroClaro = desencriptar();

        if (numeroClaro.length() < 4) {
            return "****";
        }

        String ultimos4 = numeroClaro.substring(numeroClaro.length() - 4);
        int cantidadAsteriscos = numeroClaro.length() - 4;

        StringBuilder enmascarado = new StringBuilder();
        for (int i = 0; i < cantidadAsteriscos; i++) {
            if (i > 0 && i % 4 == 0) {
                enmascarado.append(" ");
            }
            enmascarado.append("*");
        }

        if (cantidadAsteriscos > 0) {
            enmascarado.append(" ");
        }

        for (int i = 0; i < ultimos4.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                enmascarado.append(" ");
            }
            enmascarado.append(ultimos4.charAt(i));
        }

        return enmascarado.toString();
    }

    /**
     * Detecta el tipo de tarjeta a partir del número.
     *
     * @return tipo de tarjeta detectado, o null si no se reconoce
     */
    public TipoTarjeta detectarTipoTarjeta() {
        String numeroClaro = desencriptar();
        return TipoTarjeta.detectarTipo(numeroClaro);
    }

    /**
     * Obtiene los primeros 6 dígitos (BIN) de la tarjeta.
     * Útil para identificar el banco emisor.
     *
     * @return BIN de la tarjeta
     */
    public String obtenerBIN() {
        String numeroClaro = desencriptar();
        return numeroClaro.length() >= 6 ? numeroClaro.substring(0, 6) : numeroClaro;
    }

    // ============================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ============================================

    private static void validarFormato(String numeroClaro) {
        if (StringUtils.isBlank(numeroClaro)) {
            throw new IllegalArgumentException("El número de tarjeta no puede estar vacío");
        }

        // Remover espacios y guiones
        String numeroLimpio = numeroClaro.replaceAll("[\\s-]", "");

        if (!numeroLimpio.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("El número de tarjeta solo puede contener dígitos");
        }

        if (numeroLimpio.length() < 13 || numeroLimpio.length() > 19) {
            throw new IllegalArgumentException(
                    "El número de tarjeta debe tener entre 13 y 19 dígitos. Longitud actual: " + numeroLimpio.length()
            );
        }
    }

    /**
     * Valida el número de tarjeta usando el algoritmo de Luhn.
     *
     * @param numeroClaro número de tarjeta en claro
     * @return true si es válido, false en caso contrario
     */
    private static boolean validarLuhn(String numeroClaro) {
        String numeroLimpio = numeroClaro.replaceAll("[\\s-]", "");

        int suma = 0;
        boolean alternar = false;

        // Iterar de derecha a izquierda
        for (int i = numeroLimpio.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(numeroLimpio.charAt(i));

            if (alternar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }

            suma += digito;
            alternar = !alternar;
        }

        return (suma % 10 == 0);
    }

    // ============================================
    // PLACEHOLDER DE ENCRIPTACIÓN (Base64)
    // TODO: Reemplazar con AES-256 o algoritmo robusto en producción
    // ============================================

    /**
     * Encripta el número de tarjeta usando Base64 (PLACEHOLDER).
     *
     * <p><strong>ADVERTENCIA:</strong> Base64 NO es encriptación real,
     * solo es codificación. En producción debe usarse AES-256 o similar
     * con gestión segura de claves (HSM, Key Vault, etc.).
     *
     * @param numeroClaro número en claro
     * @return número "encriptado" (codificado en Base64)
     */
    private static String encriptar(String numeroClaro) {
        // PLACEHOLDER: Codificación Base64
        // TODO: Implementar encriptación real (AES-256-GCM, RSA, etc.)
        return Base64.getEncoder().encodeToString(numeroClaro.getBytes());
    }

    /**
     * Desencripta el número de tarjeta desde Base64 (PLACEHOLDER).
     *
     * @param valorEncriptado valor "encriptado"
     * @return número en claro
     */
    private static String desencriptarPlaceholder(String valorEncriptado) {
        // PLACEHOLDER: Decodificación Base64
        // TODO: Implementar desencriptación real
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(valorEncriptado);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error al desencriptar el número de tarjeta", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumeroTarjeta that = (NumeroTarjeta) o;
        return Objects.equals(valorEncriptado, that.valorEncriptado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valorEncriptado);
    }

    @Override
    public String toString() {
        return "NumeroTarjeta{enmascarado='" + obtenerNumeroEnmascarado() + "'}";
    }
}

