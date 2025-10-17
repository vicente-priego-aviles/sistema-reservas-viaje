package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.YearMonth;

@ValueObject
public record TarjetaCredito(
        String numero,
        YearMonth fechaExpiracion,
        String cvv
) {
    public boolean esValida() {
        return validarLuhn(numero) &&
               !estaExpirada(fechaExpiracion) &&
               cvv != null && cvv.length() == 3;
    }

    private boolean validarLuhn(String numero) {
        // Algoritmo de Luhn para validar tarjeta
        String numeroLimpio = numero.replaceAll("\\D", "");
        if (numeroLimpio.length() < 13 || numeroLimpio.length() > 19) {
            return false;
        }

        int suma = 0;
        boolean alternar = false;

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

    private boolean estaExpirada(YearMonth fechaExpiracion) {
        return fechaExpiracion.isBefore(YearMonth.now());
    }
}