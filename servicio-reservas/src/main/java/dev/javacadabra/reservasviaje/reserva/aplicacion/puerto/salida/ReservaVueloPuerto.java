package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;

import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de Pagos de vuelo.
 * Define el contrato que debe implementar el adaptador de persistencia.
 */
public interface ReservaVueloPuerto {

    /**
     * Guarda una reserva de vuelo.
     *
     * @param reservaVuelo agregado de reserva de vuelo a guardar
     * @return la reserva guardada con ID asignado
     */
    ReservaVuelo guardar(ReservaVuelo reservaVuelo);

    /**
     * Busca una reserva de vuelo por su ID.
     *
     * @param reservaId ID de la reserva
     * @return Optional con la reserva si existe, vacío si no
     */
    Optional<ReservaVuelo> buscarPorId(ReservaId reservaId);

    /**
     * Verifica si existe una reserva con el ID dado.
     *
     * @param reservaId ID de la reserva
     * @return true si existe, false si no
     */
    boolean existePorId(ReservaId reservaId);

    /**
     * Elimina una reserva de vuelo.
     *
     * @param reservaId ID de la reserva a eliminar
     */
    void eliminar(ReservaId reservaId);
}