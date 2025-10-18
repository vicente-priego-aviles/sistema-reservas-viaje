package dev.javacadabra.reservasviaje.pago.dominio.excepcion;

public class MontoExcedeLimiteException extends RuntimeException {

    public MontoExcedeLimiteException(Double monto) {
        super(String.format("El monto %.2f excede el límite permitido de 10000", monto));
    }
}