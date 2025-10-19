package dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.salida;

import dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.ReservaHotelId;

import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de reservas de hotel.
 *
 * <p>Define el contrato que debe implementar el adaptador de persistencia.
 * Siguiendo arquitectura hexagonal, el dominio NO depende de la implementación
 * de infraestructura.</p>
 */
public interface ReservaHotelRepositorioPuertoSalida {

    /**
     * Guarda una reserva de hotel.
     *
     * @param reserva Reserva a guardar
     * @return La reserva guardada
     */
    ReservaHotel guardar(ReservaHotel reserva);

    /**
     * Busca una reserva por su ID.
     *
     * @param id ID de la reserva
     * @return Optional con la reserva si existe
     */
    Optional<ReservaHotel> buscarPorId(ReservaHotelId id);

    /**
     * Busca una reserva por el ID de la reserva de viaje.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return Optional con la reserva si existe
     */
    Optional<ReservaHotel> buscarPorReservaViajeId(String reservaViajeId);

    /**
     * Elimina una reserva.
     *
     * @param id ID de la reserva a eliminar
     */
    void eliminar(ReservaHotelId id);

    /**
     * Verifica si existe una reserva para el ID de reserva de viaje dado.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return true si existe, false en caso contrario
     */
    boolean existePorReservaViajeId(String reservaViajeId);
}