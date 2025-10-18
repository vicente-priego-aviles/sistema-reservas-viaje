package dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia;

import dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.entidad.ReservaVueloJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservaVueloRepositorioJpa extends JpaRepository<ReservaVueloJpaEntity, String> {
    Optional<ReservaVueloJpaEntity> findByReservaViajeId(String reservaViajeId);
}