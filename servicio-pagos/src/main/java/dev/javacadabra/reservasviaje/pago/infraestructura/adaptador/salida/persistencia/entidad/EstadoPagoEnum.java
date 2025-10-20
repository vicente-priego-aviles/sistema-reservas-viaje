package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.entidad;

/**
 * Enum de persistencia para el estado de un pago.
 *
 * <p>Este enum es específico de la capa de infraestructura.
 * El dominio tiene su propio enum: EstadoPago.</p>
 */
public enum EstadoPagoEnum {
    PENDIENTE,
    PROCESANDO,
    PROCESADO,
    CONFIRMADO,
    FALLIDO,
    REVERTIDO
}