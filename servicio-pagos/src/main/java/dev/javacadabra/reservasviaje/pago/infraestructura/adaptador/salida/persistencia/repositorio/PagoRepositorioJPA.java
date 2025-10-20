package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.entidad.PagoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para PagoEntidad.
 *
 * <p>Spring Data JPA genera automáticamente la implementación
 * en tiempo de ejecución.</p>
 */
@Repository
public interface PagoRepositorioJPA extends JpaRepository<PagoEntidad, String> {

    /**
     * Busca un pago por el ID de la reserva de viaje.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return Optional con el pago si existe
     */
    Optional<PagoEntidad> findByReservaViajeId(String reservaViajeId);

    /**
     * Verifica si existe un pago para el ID de reserva de viaje dado.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return true si existe, false en caso contrario
     */
    boolean existsByReservaViajeId(String reservaViajeId);
}