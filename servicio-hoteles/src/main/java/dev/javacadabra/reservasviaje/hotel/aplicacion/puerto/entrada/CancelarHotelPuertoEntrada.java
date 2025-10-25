package dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.entrada;

/**
 * Puerto de entrada para cancelar Hoteles de hotel.
 *
 * <p>Define el contrato para el caso de uso de cancelación de hotel.
 * Siguiendo arquitectura hexagonal, este puerto es implementado
 * por un servicio de aplicación.</p>
 */
public interface CancelarHotelPuertoEntrada {

    /**
     * Cancela una reserva de hotel.
     *
     * @param reservaId ID de la reserva de viaje
     */
    void cancelarHotel(String reservaId);
}
