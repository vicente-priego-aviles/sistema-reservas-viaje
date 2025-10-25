package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;

import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de Pagos de hotel.
 * Define el contrato que debe implementar el adaptador de persistencia.
 */
public interface ReservaHotelPuerto {

    /**
     * Guarda una reserva de hotel.
     *
     * @param reservaHotel agregado de reserva de hotel a guardar
     * @return la reserva guardada con ID asignado
     */
    ReservaHotel guardar(ReservaHotel reservaHotel);

    /**
     * Busca una reserva de hotel por su ID.
     *
     * @param reservaId ID de la reserva
     * @return Optional con la reserva si existe, vacío si no
     */
    Optional<ReservaHotel> buscarPorId(ReservaId reservaId);

    /**
     * Verifica si existe una reserva con el ID dado.
     *
     * @param reservaId ID de la reserva
     * @return true si existe, false si no
     */
    boolean existePorId(ReservaId reservaId);

    /**
     * Elimina una reserva de hotel.
     *
     * @param reservaId ID de la reserva a eliminar
     */
    void eliminar(ReservaId reservaId);
}
