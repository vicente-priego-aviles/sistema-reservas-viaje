package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada;

/**
 * Puerto de entrada para el caso de uso de cancelar reserva de hotel.
 * Define el contrato que debe cumplir cualquier implementación.
 */
public interface CancelarHotelCasoUso {

    /**
     * Ejecuta el caso de uso de cancelar una reserva de hotel.
     *
     * @param reservaId ID de la reserva a cancelar
     * @param motivo motivo de la cancelación
     */
    void ejecutar(String reservaId, String motivo);
}
