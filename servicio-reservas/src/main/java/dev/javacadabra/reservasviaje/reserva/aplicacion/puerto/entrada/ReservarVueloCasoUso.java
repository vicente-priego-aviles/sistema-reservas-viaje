package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarVueloDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaVueloRespuestaDTO;

/**
 * Puerto de entrada para el caso de uso de reservar vuelo.
 * Define el contrato que debe cumplir cualquier implementación.
 */
public interface ReservarVueloCasoUso {

    /**
     * Ejecuta el caso de uso de reservar un vuelo.
     *
     * @param dto datos de entrada para crear la reserva de vuelo
     * @return datos de la reserva creada
     */
    ReservaVueloRespuestaDTO ejecutar(ReservarVueloDTO dto);
}
