package dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.salida.persistencia.entidad.ReservaHotelEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para ReservaHotelEntidad.
 *
 * <p>Spring Data JPA genera automáticamente la implementación
 * en tiempo de ejecución.</p>
 */
@Repository
public interface ReservaHotelRepositorioJPA extends JpaRepository<ReservaHotelEntidad, String> {

    /**
     * Busca una reserva de hotel por el ID de la reserva de viaje.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return Optional con la reserva si existe
     */
    Optional<ReservaHotelEntidad> findByReservaViajeId(String reservaViajeId);

    /**
     * Verifica si existe una reserva para el ID de reserva de viaje dado.
     *
     * @param reservaViajeId ID de la reserva de viaje
     * @return true si existe, false en caso contrario
     */
    boolean existsByReservaViajeId(String reservaViajeId);
}
