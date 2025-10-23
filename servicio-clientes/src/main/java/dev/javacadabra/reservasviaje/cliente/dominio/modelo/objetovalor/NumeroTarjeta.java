package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Base64;

/**
 * Value Object que representa un número de tarjeta de crédito.
 *
 * <p>El número se almacena encriptado por seguridad y nunca se expone
 * en texto plano excepto los últimos 4 dígitos para visualización.
 *
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>El número completo se encripta con Base64 (en producción usar AES-256)</li>
 *   <li>Solo se exponen los últimos 4 dígitos enmascarados</li>
 *   <li>Se valida con el algoritmo de Luhn</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
@Getter
@EqualsAndHashCode
public class NumeroTarjeta {

    /**
     * Número de tarjeta encriptado (Base64).
     */
    private final String valorEncriptado;

    /**
     * Últimos 4 dígitos (para mostrar enmascarado).
     */
    private final String ultimosDigitos;

    /**
     * Primeros dígitos (para detectar tipo de tarjeta).
     */
    private final String primerosDigitos;

    // ==================== CONSTRUCTORES ====================

    private NumeroTarjeta(String valorEncriptado, String ultimosDigitos, String primerosDigitos) {
        this.valorEncriptado = valorEncriptado;
        this.ultimosDigitos = ultimosDigitos;
        this.primerosDigitos = primerosDigitos;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Crea un NumeroTarjeta desde texto plano (para nuevas tarjetas).
     *
     * @param numeroPlano número de tarjeta en texto plano
     * @return NumeroTarjeta validado y encriptado
     * @throws IllegalArgumentException si el número no es válido
     */
    public static NumeroTarjeta de(String numeroPlano) {
        validar(numeroPlano);

        String numeroLimpio = numeroPlano.replaceAll("\\s|-", "");

        return new NumeroTarjeta(
                encriptar(numeroLimpio),
                extraerUltimosDigitos(numeroLimpio),
                extraerPrimerosDigitos(numeroLimpio)
        );
    }

    /**
     * Reconstruye un NumeroTarjeta desde persistencia (ya encriptado).
     *
     * <p>Este método se usa para reconstruir desde la base de datos,
     * donde el número ya está almacenado encriptado.
     *
     * @param valorEncriptado número ya encriptado en Base64
     * @return NumeroTarjeta reconstruido
     * @throws IllegalArgumentException si el valor encriptado no es válido
     */
    public static NumeroTarjeta reconstruido(String valorEncriptado) {
        if (valorEncriptado == null || valorEncriptado.isBlank()) {
            throw new IllegalArgumentException("El número encriptado no puede estar vacío");
        }

        try {
            // Desencriptar temporalmente para extraer dígitos
            String numeroPlano = desencriptar(valorEncriptado);

            return new NumeroTarjeta(
                    valorEncriptado,
                    extraerUltimosDigitos(numeroPlano),
                    extraerPrimerosDigitos(numeroPlano)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("El valor encriptado no es válido: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Obtiene el número enmascarado para mostrar (**** **** **** 1234).
     *
     * @return número enmascarado
     */
    public String obtenerNumeroEnmascarado() {
        return "**** **** **** " + ultimosDigitos;
    }

    /**
     * Detecta el tipo de tarjeta según los primeros dígitos.
     *
     * @return tipo de tarjeta detectado o OTRA si no se puede detectar
     */
    public TipoTarjeta detectarTipoTarjeta() {
        return TipoTarjeta.detectarDesdeNumero(primerosDigitos);
    }

    /**
     * Desencripta el número completo (solo para uso interno).
     *
     * @return número desencriptado
     */
    public String desencriptarNumero() {
        return desencriptar(valorEncriptado);
    }

    // ==================== VALIDACIONES ====================

    private static void validar(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de tarjeta es obligatorio");
        }

        String numeroLimpio = numero.replaceAll("\\s|-", "");

        if (!numeroLimpio.matches("\\d+")) {
            throw new IllegalArgumentException("El número de tarjeta solo puede contener dígitos");
        }

        if (numeroLimpio.length() < 13 || numeroLimpio.length() > 19) {
            throw new IllegalArgumentException(
                    "El número de tarjeta debe tener entre 13 y 19 dígitos"
            );
        }

        if (!validarLuhn(numeroLimpio)) {
            throw new IllegalArgumentException("El número de tarjeta no es válido (algoritmo de Luhn)");
        }
    }

    /**
     * Algoritmo de Luhn para validar números de tarjeta.
     *
     * @param numero número de tarjeta sin espacios
     * @return true si es válido según Luhn
     */
    private static boolean validarLuhn(String numero) {
        int suma = 0;
        boolean alternar = false;

        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(numero.charAt(i));

            if (alternar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }

            suma += digito;
            alternar = !alternar;
        }

        return suma % 10 == 0;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private static String extraerUltimosDigitos(String numero) {
        return numero.length() >= 4
                ? numero.substring(numero.length() - 4)
                : numero;
    }

    private static String extraerPrimerosDigitos(String numero) {
        return numero.length() >= 6
                ? numero.substring(0, 6)
                : numero.substring(0, Math.min(4, numero.length()));
    }

    /**
     * Encripta el número de tarjeta usando Base64.
     *
     * <p><strong>NOTA:</strong> En producción, usar AES-256 con clave segura
     * almacenada en un gestor de secretos (AWS KMS, Azure Key Vault, etc.).
     *
     * @param numero número en texto plano
     * @return número encriptado en Base64
     */
    private static String encriptar(String numero) {
        // ADVERTENCIA: Esta es una encriptación básica solo para demostración
        // En producción, implementar AES-256-GCM con clave rotativa
        return Base64.getEncoder().encodeToString(numero.getBytes());
    }

    /**
     * Desencripta el número de tarjeta desde Base64.
     *
     * @param encriptado número encriptado
     * @return número en texto plano
     */
    private static String desencriptar(String encriptado) {
        return new String(Base64.getDecoder().decode(encriptado));
    }

    @Override
    public String toString() {
        return "NumeroTarjeta{" +
               "enmascarado='" + obtenerNumeroEnmascarado() + '\'' +
               '}';
    }
}