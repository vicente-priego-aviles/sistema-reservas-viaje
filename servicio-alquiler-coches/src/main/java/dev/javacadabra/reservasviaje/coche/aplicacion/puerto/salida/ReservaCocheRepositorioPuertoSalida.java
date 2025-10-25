package dev.javacadabra.reservasviaje.coche.aplicacion.puerto.salida;

import dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.ReservaCocheId;

import java.util.Optional;

/**
 * Puerto de salida para el repositorio de Pagos de coches.
 * Define las operaciones de persistencia necesarias desde el dominio.
 *
 * <p>Siguiendo arquitectura hexagonal, esta interfaz pertenece a la capa
 * de aplicación y será implementada por un adaptador en la capa de infraestructura.</p>
 */
public interface ReservaCocheRepositorioPuertoSalida {

    /**
     * Guarda o actualiza una reserva de coche.
     *
     * @param reserva la reserva a persistir
     * @return la reserva guardada con los datos actualizados
     */
    ReservaCoche guardar(ReservaCoche reserva);

    /**
     * Busca una reserva por su identificador único.
     *
     * @param id el identificador de la reserva
     * @return Optional con la reserva si existe, vacío si no
     */
    Optional<ReservaCoche> buscarPorId(ReservaCocheId id);

    /**
     * Busca una reserva por el ID de la reserva de viaje asociada.
     *
     * @param reservaViajeId el identificador de la reserva de viaje principal
     * @return Optional con la reserva si existe, vacío si no
     */
    Optional<ReservaCoche> buscarPorReservaViajeId(String reservaViajeId);

    /**
     * Elimina una reserva por su identificador.
     *
     * @param id el identificador de la reserva a eliminar
     */
    void eliminar(ReservaCocheId id);

    /**
     * Verifica si existe una reserva para una reserva de viaje específica.
     *
     * @param reservaViajeId el identificador de la reserva de viaje
     * @return true si existe, false en caso contrario
     */
    boolean existePorReservaViajeId(String reservaViajeId);
}
