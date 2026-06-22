package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaHotelEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservaHotelJpaRepositorio extends JpaRepository<ReservaHotelEntidad, String> {

    /**
     * Busca una reserva de hotel por su ID de negocio (ReservaId).
     *
     * @param reservaId ID de la reserva (UUID)
     * @return Optional con la entidad si existe
     */
    Optional<ReservaHotelEntidad> findByReservaId(String reservaId);

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
