package dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para ReservaCocheEntidad.
 *
 * <p>Proporciona acceso a datos mediante Spring Data JPA,
 * aprovechando los métodos derivados del nombre y queries personalizadas.</p>
 */
@Repository
public interface ReservaCocheRepositorioJPA extends JpaRepository<ReservaCocheEntidad, String> {

    /**
     * Busca una reserva por el ID de la reserva de viaje.
     *
     * <p>Spring Data JPA genera automáticamente la query a partir del nombre del método.</p>
     *
     * @param reservaViajeId el identificador de la reserva de viaje
     * @return Optional con la reserva si existe
     */
    Optional<ReservaCocheEntidad> findByReservaViajeId(String reservaViajeId);

    /**
     * Verifica si existe una reserva para una reserva de viaje específica.
     *
     * @param reservaViajeId el identificador de la reserva de viaje
     * @return true si existe, false en caso contrario
     */
    boolean existsByReservaViajeId(String reservaViajeId);

    /**
     * Busca reservas por matrícula del vehículo.
     * Útil para verificar disponibilidad o historial.
     *
     * @param matricula la matrícula del vehículo
     * @return Optional con la reserva si existe
     */
    @Query("SELECT r FROM ReservaCocheEntidad r WHERE r.matricula = :matricula")
    Optional<ReservaCocheEntidad> buscarPorMatricula(@Param("matricula") String matricula);
}
