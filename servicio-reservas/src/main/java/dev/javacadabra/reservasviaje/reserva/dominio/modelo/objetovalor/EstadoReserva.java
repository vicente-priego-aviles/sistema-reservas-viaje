package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

public enum EstadoReserva {
    INICIADA,
    VALIDANDO_DATOS,
    VALIDANDO_CLIENTE,
    EN_PROCESO_RESERVAS,
    EN_PROCESO_PAGO,
    CONFIRMADA,
    FALLIDA,
    CANCELADA
}
