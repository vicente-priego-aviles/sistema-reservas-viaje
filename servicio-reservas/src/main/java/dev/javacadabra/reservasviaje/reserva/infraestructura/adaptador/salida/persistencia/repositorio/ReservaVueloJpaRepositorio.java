package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaVueloEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para operaciones de persistencia de reservas de vuelo.
 */
@Repository
public interface ReservaVueloJpaRepositorio extends JpaRepository<ReservaVueloEntidad, Long> {

    /**
     * Busca una reserva de vuelo por su ID de negocio (ReservaId).
     *
     * @param reservaId ID de la reserva (UUID)
     * @return Optional con la entidad si existe
     */
    Optional<ReservaVueloEntidad> findByReservaId(String reservaId);

    /**
     * Verifica si existe una reserva con el ID dado.
     *
     * @param reservaId ID de la reserva
     * @return true si existe, false si no
     */
    boolean existsByReservaId(String reservaId);

    /**
     * Busca una reserva de vuelo con sus pasajeros cargados (eager loading).
     *
     * @param reservaId ID de la reserva
     * @return Optional con la entidad y pasajeros cargados
     */
    @Query("SELECT r FROM ReservaVueloEntidad r LEFT JOIN FETCH r.pasajeros WHERE r.reservaId = :reservaId")
    Optional<ReservaVueloEntidad> findByReservaIdWithPasajeros(@Param("reservaId") String reservaId);

    /**
     * Elimina una reserva por su ID de negocio.
     *
     * @param reservaId ID de la reserva
     */
    void deleteByReservaId(String reservaId);
}
