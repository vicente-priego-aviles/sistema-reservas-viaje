package dev.javacadabra.reservasviaje.pago.dominio.excepcion;

public class MontoInvalidoException extends RuntimeException {

    public MontoInvalidoException(Double monto) {
        super(String.format("Monto inválido: %.2f", monto));
    }
}
