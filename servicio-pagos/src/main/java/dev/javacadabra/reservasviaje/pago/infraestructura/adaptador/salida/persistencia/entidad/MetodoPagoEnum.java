package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.entidad;

/**
 * Enum de persistencia para el método de pago.
 *
 * <p>Este enum es específico de la capa de infraestructura.
 * El dominio tiene su propio enum: MetodoPago.</p>
 */
public enum MetodoPagoEnum {
    TARJETA_CREDITO,
    TARJETA_DEBITO,
    TRANSFERENCIA,
    PAYPAL
}