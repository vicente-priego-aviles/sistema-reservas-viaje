package dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor;

public enum EstadoPago {
    PENDIENTE,
    PROCESANDO,
    PROCESADO,
    CONFIRMADO,
    FALLIDO,
    REVERTIDO
}
