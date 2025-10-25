package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para operaciones de persistencia de Pagos de coche.
 */
@Repository
public interface ReservaCocheJpaRepositorio extends JpaRepository<ReservaCocheEntidad, Long> {

    /**
     * Busca una reserva de coche por su ID de negocio (ReservaId).
     *
     * @param reservaId ID de la reserva (UUID)
     * @return Optional con la entidad si existe
     */
    Optional<ReservaCocheEntidad> findByReservaId(String reservaId);

    /**
     * Verifica si existe una reserva con el ID dado.
     *
     * @param reservaId ID de la reserva
     * @return true si existe, false si no
     */
    boolean existsByReservaId(String reservaId);

    /**
     * Elimina una reserva por su ID de negocio.
     *
     * @param reservaId ID de la reserva
     */
    void deleteByReservaId(String reservaId);
}