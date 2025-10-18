package dev.javacadabra.reservasviaje.vuelo.dominio.excepcion;

public class ReservaVueloNoEncontradaException extends RuntimeException {

    public ReservaVueloNoEncontradaException(String id) {
        super(String.format("Reserva de vuelo no encontrada: %s", id));
    }
}
