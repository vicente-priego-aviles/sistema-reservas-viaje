package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.adaptador;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaHotelPuerto;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaHotelEntidad;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador.ReservaHotelMapeador;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.repositorio.ReservaHotelJpaRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de persistencia para Pagos de hotel.
 * Implementa el puerto de salida ReservaHotelPuerto.
 * Traduce entre el dominio y la infraestructura JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaHotelAdaptador implements ReservaHotelPuerto {

    private final ReservaHotelJpaRepositorio repositorio;
    private final ReservaHotelMapeador mapeador;

    @Override
    public ReservaHotel guardar(ReservaHotel reservaHotel) {
        log.debug("💾 Guardando reserva de hotel: {}", reservaHotel.getReservaId().getValor());

        // 1. Convertir agregado de dominio a entidad JPA
        ReservaHotelEntidad entidad = mapeador.aEntidad(reservaHotel);

        // 2. Guardar en base de datos
        ReservaHotelEntidad entidadGuardada = repositorio.save(entidad);

        log.debug("✅ Reserva de hotel guardada con ID : {}", entidadGuardada.getReservaId());

        // 3. Convertir entidad JPA de vuelta a dominio
        return mapeador.aDominio(entidadGuardada);
    }

    @Override
    public Optional<ReservaHotel> buscarPorId(ReservaId reservaId) {
        log.debug("🔍 Buscando reserva de hotel: {}", reservaId.getValor());

        Optional<ReservaHotelEntidad> entidadOpt =
                repositorio.findByReservaId(reservaId.getValor());

        if (entidadOpt.isEmpty()) {
            log.debug("⚠️ Reserva de hotel no encontrada: {}", reservaId.getValor());
            return Optional.empty();
        }

        log.debug("✅ Reserva de hotel encontrada: {}", reservaId.getValor());

        return entidadOpt.map(mapeador::aDominio);
    }

    @Override
    public boolean existePorId(ReservaId reservaId) {
        log.debug("🔍 Verificando existencia de reserva de hotel: {}", reservaId.getValor());

        boolean existe = repositorio.existsByReservaId(reservaId.getValor());

        log.debug(existe ? "✅ La reserva existe" : "⚠️ La reserva no existe");

        return existe;
    }

    @Override
    public void eliminar(ReservaId reservaId) {
        log.debug("🗑️ Eliminando reserva de hotel: {}", reservaId.getValor());

        repositorio.deleteByReservaId(reservaId.getValor());

        log.debug("✅ Reserva de hotel eliminada: {}", reservaId.getValor());
    }
}