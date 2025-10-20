package dev.javacadabra.reservasviaje.pago.aplicacion.puerto.salida;

import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.PagoId;

import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de pagos.
 *
 * <p>Define el contrato que debe implementar el adaptador de persistencia.
 * Siguiendo arquitectura hexagonal, el dominio NO depende de la implementación
 * de infraestructura.</p>
 */
public interface PagoRepositorioPuertoSalida {

    /**
     * Guarda un pago.
     *
     * @param pago Pago a guardar
     * @return El pago guardado
     */
    Pago guardar(Pago pago);

    /**
     * Busca un pago por su ID.
     *
     * @param id ID del pago
     * @return Optional con el pago si existe
     */
    Optional<Pago> buscarPorId(PagoId id);

    /**
     * Busca un pago por el ID de la reserva de viaje.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return Optional con el pago si existe
     */
    Optional<Pago> buscarPorReservaViajeId(String reservaViajeId);

    /**
     * Elimina un pago.
     *
     * @param id ID del pago a eliminar
     */
    void eliminar(PagoId id);

    /**
     * Verifica si existe un pago para el ID de reserva de viaje dado.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return true si existe, false en caso contrario
     */
    boolean existePorReservaViajeId(String reservaViajeId);
}