package dev.javacadabra.reservasviaje.pago.dominio.excepcion;

public class PagoNoEncontradoException extends RuntimeException {

    public PagoNoEncontradoException(String id) {
        super(String.format("Pago no encontrado: %s", id));
    }
}
