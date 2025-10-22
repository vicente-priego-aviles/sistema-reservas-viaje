package dev.javacadabra.reservasviaje.reserva.dominio.excepcion;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;

/**
 * Excepción lanzada cuando se intenta crear una reserva que ya existe.
 */
public class ReservaDuplicadaException extends RuntimeException {

    private static final String MENSAJE_POR_DEFECTO = "La reserva ya existe en el sistema";

    public ReservaDuplicadaException() {
        super(MENSAJE_POR_DEFECTO);
    }

    public ReservaDuplicadaException(String mensaje) {
        super(mensaje);
    }

    public ReservaDuplicadaException(ReservaId reservaId) {
        super(String.format("Ya existe una reserva con el ID: %s", reservaId.getValor()));
    }

    public ReservaDuplicadaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static ReservaDuplicadaException conId(ReservaId reservaId) {
        return new ReservaDuplicadaException(reservaId);
    }

    public static ReservaDuplicadaException conMensaje(String mensaje) {
        return new ReservaDuplicadaException(mensaje);
    }
}