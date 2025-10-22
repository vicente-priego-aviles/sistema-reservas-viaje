package dev.javacadabra.reservasviaje.reserva.dominio.excepcion;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;

/**
 * Excepción lanzada cuando no se encuentra una reserva en el sistema.
 */
public class ReservaNoEncontradaException extends RuntimeException {

    private static final String MENSAJE_POR_DEFECTO = "Reserva no encontrada";

    public ReservaNoEncontradaException() {
        super(MENSAJE_POR_DEFECTO);
    }

    public ReservaNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public ReservaNoEncontradaException(ReservaId reservaId) {
        super(String.format("Reserva no encontrada con ID: %s", reservaId.getValor()));
    }

    public ReservaNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static ReservaNoEncontradaException conId(ReservaId reservaId) {
        return new ReservaNoEncontradaException(reservaId);
    }

    public static ReservaNoEncontradaException conMensaje(String mensaje) {
        return new ReservaNoEncontradaException(mensaje);
    }
}