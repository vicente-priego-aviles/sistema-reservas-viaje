package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;

/**
 * Código CVV de una tarjeta de crédito encriptado.
 *
 * <p>Value object inmutable que encapsula el código CVV de una tarjeta,
 * asegurando:
 * <ul>
 *   <li>Validación del formato (3 o 4 dígitos)</li>
 *   <li>Encriptación del código (placeholder con Base64)</li>
 *   <li>Enmascaramiento para logs seguros</li>
 * </ul>
 *
 * <p><strong>NOTA:</strong> La encriptación actual es un placeholder (Base64).
 * En producción debe reemplazarse con un algoritmo robusto (AES-256, RSA, etc.)
 * y gestión segura de claves.
 *
 * <p><strong>ADVERTENCIA DE SEGURIDAD:</strong> El CVV NUNCA debe almacenarse
 * según las normas PCI DSS. Este value object existe solo para procesamiento
 * temporal durante validaciones y transacciones.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
public record CVV(String valorEncriptado) implements Serializable {

    /**
     * Constructor canónico con validación.
     *
     * @param valorEncriptado CVV encriptado
     * @throws IllegalArgumentException si el valor es inválido
     */
    public CVV {
        if (StringUtils.isBlank(valorEncriptado)) {
            throw new IllegalArgumentException("El CVV encriptado no puede estar vacío");
        }
    }

    /**
     * Crea un CVV a partir del código en claro.
     * Valida el formato y lo encripta antes de almacenarlo.
     *
     * @param cvvClaro código CVV en claro (3 o 4 dígitos)
     * @return CVV con el valor encriptado
     * @throws IllegalArgumentException si el CVV es inválido
     */
    public static CVV crear(String cvvClaro) {
        validarFormato(cvvClaro);
        String encriptado = encriptar(cvvClaro);
        return new CVV(encriptado);
    }

    /**
     * Crea un CVV desde un valor ya encriptado.
     * Útil al recuperar datos temporalmente de memoria.
     *
     * <p><strong>RECORDATORIO:</strong> El CVV NO debe persistirse en BD
     * según PCI DSS.
     *
     * @param valorEncriptado CVV encriptado
     * @return CVV
     */
    public static CVV desdeEncriptado(String valorEncriptado) {
        return new CVV(valorEncriptado);
    }

    /**
     * Desencripta y obtiene el CVV en claro.
     *
     * <p><strong>ADVERTENCIA:</strong> Usar con extrema precaución.
     * Solo desencriptar cuando sea absolutamente necesario para validación
     * o procesamiento de pago. NUNCA loguear el valor.
     *
     * @return CVV desencriptado
     */
    public String desencriptar() {
        return desencriptarPlaceholder(valorEncriptado);
    }

    /**
     * Obtiene el CVV enmascarado para logs seguros.
     * Formato: ***
     *
     * @return CVV completamente enmascarado
     */
    public String obtenerCVVEnmascarado() {
        String cvvClaro = desencriptar();
        return "*".repeat(cvvClaro.length());
    }

    /**
     * Obtiene la longitud del CVV (3 o 4).
     *
     * @return longitud del CVV
     */
    public int obtenerLongitud() {
        return desencriptar().length();
    }

    /**
     * Verifica si el CVV tiene la longitud esperada para un tipo de tarjeta.
     *
     * @param longitudEsperada longitud esperada (3 para Visa/MC, 4 para Amex)
     * @return true si coincide, false en caso contrario
     */
    public boolean validarLongitud(int longitudEsperada) {
        return obtenerLongitud() == longitudEsperada;
    }

    // ============================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ============================================

    private static void validarFormato(String cvvClaro) {
        if (StringUtils.isBlank(cvvClaro)) {
            throw new IllegalArgumentException("El CVV no puede estar vacío");
        }

        String cvvLimpio = cvvClaro.trim();

        if (!cvvLimpio.matches("^[0-9]{3,4}$")) {
            throw new IllegalArgumentException(
                    "El CVV debe contener 3 o 4 dígitos numéricos. Valor recibido: " + cvvLimpio.length() + " caracteres"
            );
        }
    }

    // ============================================
    // PLACEHOLDER DE ENCRIPTACIÓN (Base64)
    // TODO: Reemplazar con AES-256 o algoritmo robusto en producción
    // ============================================

    /**
     * Encripta el CVV usando Base64 (PLACEHOLDER).
     *
     * <p><strong>ADVERTENCIA:</strong> Base64 NO es encriptación real,
     * solo es codificación. En producción debe usarse AES-256-GCM o similar
     * con gestión segura de claves.
     *
     * <p><strong>IMPORTANTE:</strong> Según PCI DSS, el CVV NO debe almacenarse
     * después de la autorización de la transacción.
     *
     * @param cvvClaro CVV en claro
     * @return CVV "encriptado" (codificado en Base64)
     */
    private static String encriptar(String cvvClaro) {
        // PLACEHOLDER: Codificación Base64
        // TODO: Implementar encriptación real (AES-256-GCM)
        return Base64.getEncoder().encodeToString(cvvClaro.getBytes());
    }

    /**
     * Desencripta el CVV desde Base64 (PLACEHOLDER).
     *
     * @param valorEncriptado valor "encriptado"
     * @return CVV en claro
     */
    private static String desencriptarPlaceholder(String valorEncriptado) {
        // PLACEHOLDER: Decodificación Base64
        // TODO: Implementar desencriptación real
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(valorEncriptado);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error al desencriptar el CVV", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CVV cvv = (CVV) o;
        return Objects.equals(valorEncriptado, cvv.valorEncriptado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valorEncriptado);
    }

    @Override
    public String toString() {
        return "CVV{enmascarado='" + obtenerCVVEnmascarado() + "'}";
    }
}

