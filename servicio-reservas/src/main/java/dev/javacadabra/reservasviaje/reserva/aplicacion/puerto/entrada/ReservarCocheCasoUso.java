package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarCocheDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaCocheRespuestaDTO;

/**
 * Puerto de entrada para el caso de uso de reservar coche.
 * Define el contrato que debe cumplir cualquier implementación.
 */
public interface ReservarCocheCasoUso {

    /**
     * Ejecuta el caso de uso de reservar un coche de alquiler.
     *
     * @param dto datos de entrada para crear la reserva de coche
     * @return datos de la reserva creada
     */
    ReservaCocheRespuestaDTO ejecutar(ReservarCocheDTO dto);
}
