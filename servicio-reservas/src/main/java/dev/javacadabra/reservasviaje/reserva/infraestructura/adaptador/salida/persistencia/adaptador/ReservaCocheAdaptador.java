package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.adaptador;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaCochePuerto;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador.ReservaCocheMapeador;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.repositorio.ReservaCocheJpaRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de persistencia para reservas de coche.
 * Implementa el puerto de salida ReservaCochePuerto.
 * Traduce entre el dominio y la infraestructura JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaCocheAdaptador implements ReservaCochePuerto {

    private final ReservaCocheJpaRepositorio repositorio;
    private final ReservaCocheMapeador mapeador;

    @Override
    public ReservaCoche guardar(ReservaCoche reservaCoche) {
        log.debug("💾 Guardando reserva de coche: {}", reservaCoche.getReservaId().getValor());

        // 1. Convertir agregado de dominio a entidad JPA
        ReservaCocheEntidad entidad = mapeador.aEntidad(reservaCoche);

        // 2. Guardar en base de datos
        ReservaCocheEntidad entidadGuardada = repositorio.save(entidad);

        log.debug("✅ Reserva de coche guardada con ID técnico: {}", entidadGuardada.getId());

        // 3. Convertir entidad JPA de vuelta a dominio
        return mapeador.aDominio(entidadGuardada);
    }

    @Override
    public Optional<ReservaCoche> buscarPorId(ReservaId reservaId) {
        log.debug("🔍 Buscando reserva de coche: {}", reservaId.getValor());

        Optional<ReservaCocheEntidad> entidadOpt =
                repositorio.findByReservaId(reservaId.getValor());

        if (entidadOpt.isEmpty()) {
            log.debug("⚠️ Reserva de coche no encontrada: {}", reservaId.getValor());
            return Optional.empty();
        }

        log.debug("✅ Reserva de coche encontrada: {}", reservaId.getValor());

        return entidadOpt.map(mapeador::aDominio);
    }

    @Override
    public boolean existePorId(ReservaId reservaId) {
        log.debug("🔍 Verificando existencia de reserva de coche: {}", reservaId.getValor());

        boolean existe = repositorio.existsByReservaId(reservaId.getValor());

        log.debug(existe ? "✅ La reserva existe" : "⚠️ La reserva no existe");

        return existe;
    }

    @Override
    public void eliminar(ReservaId reservaId) {
        log.debug("🗑️ Eliminando reserva de coche: {}", reservaId.getValor());

        repositorio.deleteByReservaId(reservaId.getValor());

        log.debug("✅ Reserva de coche eliminada: {}", reservaId.getValor());
    }
}
