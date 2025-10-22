package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarHotelDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaHotelRespuestaDTO;

/**
 * Puerto de entrada para el caso de uso de reservar hotel.
 * Define el contrato que debe cumplir cualquier implementación.
 */
public interface ReservarHotelCasoUso {

    /**
     * Ejecuta el caso de uso de reservar un hotel.
     *
     * @param dto datos de entrada para crear la reserva de hotel
     * @return datos de la reserva creada
     */
    ReservaHotelRespuestaDTO ejecutar(ReservarHotelDTO dto);
}