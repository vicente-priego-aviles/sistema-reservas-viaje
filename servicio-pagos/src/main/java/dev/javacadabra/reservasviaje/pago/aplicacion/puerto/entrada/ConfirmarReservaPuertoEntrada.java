package dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada;

/**
 * Puerto de entrada para confirmar Pagos.
 *
 * <p>Define el contrato para el caso de uso de confirmación de reserva
 * después de un pago exitoso.</p>
 */
public interface ConfirmarReservaPuertoEntrada {

    /**
     * Confirma una reserva de viaje.
     *
     * @param reservaId ID de la reserva de viaje
     * @return Número de confirmación generado
     */
    String confirmarReserva(String reservaId);
}