package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value Object que representa el precio de una reserva.
 * Es inmutable y garantiza que el precio siempre es válido y con precisión correcta.
 */
@ValueObject
@Getter
public class PrecioReserva implements Serializable, Comparable<PrecioReserva> {

    private static final Currency MONEDA_POR_DEFECTO = Currency.getInstance("EUR");
    private static final int ESCALA_DECIMAL = 2;

    private final BigDecimal monto;
    private final Currency moneda;

    /**
     * Constructor privado para forzar el uso de métodos factory.
     */
    private PrecioReserva(BigDecimal monto, Currency moneda) {
        if (monto == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (moneda == null) {
            throw new IllegalArgumentException("La moneda no puede ser nula");
        }

        this.monto = monto.setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP);
        this.moneda = moneda;
    }

    /**
     * Crea un PrecioReserva con la moneda por defecto (EUR).
     *
     * @param monto el monto del precio
     * @return nueva instancia de PrecioReserva
     */
    public static PrecioReserva de(BigDecimal monto) {
        return new PrecioReserva(monto, MONEDA_POR_DEFECTO);
    }

    /**
     * Crea un PrecioReserva con monto y moneda específicos.
     *
     * @param monto el monto del precio
     * @param moneda la moneda
     * @return nueva instancia de PrecioReserva
     */
    public static PrecioReserva de(BigDecimal monto, Currency moneda) {
        return new PrecioReserva(monto, moneda);
    }

    /**
     * Crea un PrecioReserva con monto y código de moneda.
     *
     * @param monto el monto del precio
     * @param codigoMoneda código ISO 4217 de la moneda (ej: "EUR", "USD")
     * @return nueva instancia de PrecioReserva
     */
    public static PrecioReserva de(BigDecimal monto, String codigoMoneda) {
        Currency moneda = Currency.getInstance(codigoMoneda);
        return new PrecioReserva(monto, moneda);
    }

    /**
     * Crea un PrecioReserva a partir de un double.
     *
     * @param monto el monto del precio
     * @return nueva instancia de PrecioReserva
     */
    public static PrecioReserva de(double monto) {
        return new PrecioReserva(BigDecimal.valueOf(monto), MONEDA_POR_DEFECTO);
    }

    /**
     * Crea un PrecioReserva con valor cero.
     *
     * @return nueva instancia de PrecioReserva con monto 0
     */
    public static PrecioReserva cero() {
        return new PrecioReserva(BigDecimal.ZERO, MONEDA_POR_DEFECTO);
    }

    /**
     * Suma este precio con otro.
     *
     * @param otro el otro precio a sumar
     * @return nuevo PrecioReserva con la suma
     * @throws IllegalArgumentException si las monedas no coinciden
     */
    public PrecioReserva sumar(PrecioReserva otro) {
        validarMismaMoneda(otro);
        return new PrecioReserva(this.monto.add(otro.monto), this.moneda);
    }

    /**
     * Resta otro precio de este.
     *
     * @param otro el otro precio a restar
     * @return nuevo PrecioReserva con la resta
     * @throws IllegalArgumentException si las monedas no coinciden o el resultado es negativo
     */
    public PrecioReserva restar(PrecioReserva otro) {
        validarMismaMoneda(otro);
        BigDecimal resultado = this.monto.subtract(otro.monto);

        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La resta resulta en un precio negativo");
        }

        return new PrecioReserva(resultado, this.moneda);
    }

    /**
     * Multiplica este precio por un factor.
     *
     * @param factor el factor multiplicador
     * @return nuevo PrecioReserva multiplicado
     */
    public PrecioReserva multiplicar(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El factor no puede ser negativo");
        }
        return new PrecioReserva(this.monto.multiply(factor), this.moneda);
    }

    /**
     * Aplica un porcentaje de descuento.
     *
     * @param porcentaje el porcentaje de descuento (0-100)
     * @return nuevo PrecioReserva con el descuento aplicado
     */
    public PrecioReserva aplicarDescuento(BigDecimal porcentaje) {
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0 ||
            porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }

        BigDecimal factorDescuento = BigDecimal.ONE
                .subtract(porcentaje.divide(BigDecimal.valueOf(100), ESCALA_DECIMAL, RoundingMode.HALF_UP));

        return new PrecioReserva(this.monto.multiply(factorDescuento), this.moneda);
    }

    /**
     * Verifica si el precio es cero.
     *
     * @return true si el monto es 0, false en caso contrario
     */
    public boolean esCero() {
        return monto.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Verifica si este precio es mayor que otro.
     *
     * @param otro el otro precio
     * @return true si este precio es mayor
     */
    public boolean esMayorQue(PrecioReserva otro) {
        validarMismaMoneda(otro);
        return this.monto.compareTo(otro.monto) > 0;
    }

    /**
     * Verifica si este precio es menor que otro.
     *
     * @param otro el otro precio
     * @return true si este precio es menor
     */
    public boolean esMenorQue(PrecioReserva otro) {
        validarMismaMoneda(otro);
        return this.monto.compareTo(otro.monto) < 0;
    }

    /**
     * Valida que dos precios tengan la misma moneda.
     */
    private void validarMismaMoneda(PrecioReserva otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException(
                    String.format("Las monedas no coinciden: %s vs %s",
                            this.moneda.getCurrencyCode(),
                            otro.moneda.getCurrencyCode())
            );
        }
    }

    public String getCodigoMoneda() {
        return moneda.getCurrencyCode();
    }

    @Override
    public int compareTo(PrecioReserva otro) {
        validarMismaMoneda(otro);
        return this.monto.compareTo(otro.monto);
    }
}
