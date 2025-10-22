package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;

import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de reservas de coche.
 * Define el contrato que debe implementar el adaptador de persistencia.
 */
public interface ReservaCochePuerto {

    /**
     * Guarda una reserva de coche.
     *
     * @param reservaCoche agregado de reserva de coche a guardar
     * @return la reserva guardada con ID asignado
     */
    ReservaCoche guardar(ReservaCoche reservaCoche);

    /**
     * Busca una reserva de coche por su ID.
     *
     * @param reservaId ID de la reserva
     * @return Optional con la reserva si existe, vacío si no
     */
    Optional<ReservaCoche> buscarPorId(ReservaId reservaId);

    /**
     * Verifica si existe una reserva con el ID dado.
     *
     * @param reservaId ID de la reserva
     * @return true si existe, false si no
     */
    boolean existePorId(ReservaId reservaId);

    /**
     * Elimina una reserva de coche.
     *
     * @param reservaId ID de la reserva a eliminar
     */
    void eliminar(ReservaId reservaId);
}