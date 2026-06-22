package dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.entidad.ReservaVueloJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservaVueloRepositorioJpa extends JpaRepository<ReservaVueloJpaEntity, String> {
    Optional<ReservaVueloJpaEntity> findByReservaViajeId(String reservaViajeId);
}