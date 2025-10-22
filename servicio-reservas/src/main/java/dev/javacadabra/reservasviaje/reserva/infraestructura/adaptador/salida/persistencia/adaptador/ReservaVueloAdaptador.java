package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.adaptador;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaVueloPuerto;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaVueloEntidad;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador.ReservaVueloMapeador;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.repositorio.ReservaVueloJpaRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de persistencia para reservas de vuelo.
 * Implementa el puerto de salida ReservaVueloPuerto.
 * Traduce entre el dominio y la infraestructura JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaVueloAdaptador implements ReservaVueloPuerto {

    private final ReservaVueloJpaRepositorio repositorio;
    private final ReservaVueloMapeador mapeador;

    @Override
    public ReservaVuelo guardar(ReservaVuelo reservaVuelo) {
        log.debug("💾 Guardando reserva de vuelo: {}", reservaVuelo.getReservaId().getValor());

        // 1. Convertir agregado de dominio a entidad JPA
        ReservaVueloEntidad entidad = mapeador.aEntidad(reservaVuelo);

        // 2. Establecer relaciones bidireccionales con pasajeros
        if (entidad.getPasajeros() != null) {
            entidad.getPasajeros().forEach(pasajero -> pasajero.setReservaVuelo(entidad));
        }

        // 3. Guardar en base de datos
        ReservaVueloEntidad entidadGuardada = repositorio.save(entidad);

        log.debug("✅ Reserva de vuelo guardada con ID técnico: {}", entidadGuardada.getId());

        // 4. Convertir entidad JPA de vuelta a dominio
        return mapeador.aDominio(entidadGuardada);
    }

    @Override
    public Optional<ReservaVuelo> buscarPorId(ReservaId reservaId) {
        log.debug("🔍 Buscando reserva de vuelo: {}", reservaId.getValor());

        // Usar query con FETCH JOIN para cargar pasajeros en una sola query
        Optional<ReservaVueloEntidad> entidadOpt =
                repositorio.findByReservaIdWithPasajeros(reservaId.getValor());

        if (entidadOpt.isEmpty()) {
            log.debug("⚠️ Reserva de vuelo no encontrada: {}", reservaId.getValor());
            return Optional.empty();
        }

        log.debug("✅ Reserva de vuelo encontrada: {}", reservaId.getValor());

        return entidadOpt.map(mapeador::aDominio);
    }

    @Override
    public boolean existePorId(ReservaId reservaId) {
        log.debug("🔍 Verificando existencia de reserva de vuelo: {}", reservaId.getValor());

        boolean existe = repositorio.existsByReservaId(reservaId.getValor());

        log.debug(existe ? "✅ La reserva existe" : "⚠️ La reserva no existe");

        return existe;
    }

    @Override
    public void eliminar(ReservaId reservaId) {
        log.debug("🗑️ Eliminando reserva de vuelo: {}", reservaId.getValor());

        repositorio.deleteByReservaId(reservaId.getValor());

        log.debug("✅ Reserva de vuelo eliminada: {}", reservaId.getValor());
    }
}