package dev.javacadabra.reservasviaje.vuelo.dominio.excepcion;

public class VueloNoDisponibleException extends RuntimeException {

    public VueloNoDisponibleException(String origen, String destino) {
        super(String.format("No hay vuelos disponibles de %s a %s", origen, destino));
    }
}
