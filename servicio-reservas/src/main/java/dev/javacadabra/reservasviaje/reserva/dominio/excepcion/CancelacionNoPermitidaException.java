package dev.javacadabra.reservasviaje.reserva.dominio.excepcion;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.EstadoReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;

/**
 * Excepción lanzada cuando se intenta cancelar una reserva que no puede ser cancelada.
 */
public class CancelacionNoPermitidaException extends RuntimeException {

    private static final String MENSAJE_POR_DEFECTO = "No se puede cancelar la reserva";

    public CancelacionNoPermitidaException() {
        super(MENSAJE_POR_DEFECTO);
    }

    public CancelacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }

    public CancelacionNoPermitidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static CancelacionNoPermitidaException porEstado(ReservaId reservaId, EstadoReserva estadoActual) {
        return new CancelacionNoPermitidaException(
                String.format("No se puede cancelar la reserva %s porque está en estado: %s",
                        reservaId.getValor(), estadoActual.getNombre())
        );
    }

    public static CancelacionNoPermitidaException porTiempoExpirado(ReservaId reservaId) {
        return new CancelacionNoPermitidaException(
                String.format("No se puede cancelar la reserva %s porque el tiempo permitido ha expirado",
                        reservaId.getValor())
        );
    }

    public static CancelacionNoPermitidaException porPoliticaCancelacion(String motivo) {
        return new CancelacionNoPermitidaException(
                String.format("No se puede cancelar la reserva: %s", motivo)
        );
    }

    public static CancelacionNoPermitidaException conMensaje(String mensaje) {
        return new CancelacionNoPermitidaException(mensaje);
    }
}
