package dev.javacadabra.reservasviaje.reserva.dominio.excepcion;

/**
 * Excepción lanzada cuando una reserva no cumple con las reglas de negocio.
 */
public class ReservaInvalidaException extends RuntimeException {

    private static final String MENSAJE_POR_DEFECTO = "La reserva no es válida";

    public ReservaInvalidaException() {
        super(MENSAJE_POR_DEFECTO);
    }

    public ReservaInvalidaException(String mensaje) {
        super(mensaje);
    }

    public ReservaInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static ReservaInvalidaException porDatosIncompletos() {
        return new ReservaInvalidaException("La reserva tiene datos incompletos o incorrectos");
    }

    public static ReservaInvalidaException porEstadoInvalido(String estadoActual, String estadoRequerido) {
        return new ReservaInvalidaException(
                String.format("La reserva está en estado '%s' pero se requiere estado '%s'",
                        estadoActual, estadoRequerido)
        );
    }

    public static ReservaInvalidaException porPrecioInvalido() {
        return new ReservaInvalidaException("El precio de la reserva no es válido");
    }

    public static ReservaInvalidaException conMensaje(String mensaje) {
        return new ReservaInvalidaException(mensaje);
    }
}