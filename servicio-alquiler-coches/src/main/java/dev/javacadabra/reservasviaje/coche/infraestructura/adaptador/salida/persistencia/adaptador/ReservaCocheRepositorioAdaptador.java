package dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.adaptador;

import dev.javacadabra.reservasviaje.coche.aplicacion.puerto.salida.ReservaCocheRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.ReservaCocheId;
import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.mapper.ReservaCocheMapper;
import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.repositorio.ReservaCocheRepositorioJPA;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto de repositorio.
 *
 * <p>Este adaptador traduce entre el modelo de dominio (ReservaCoche)
 * y el modelo de persistencia (ReservaCocheEntidad).</p>
 *
 * <p>Siguiendo arquitectura hexagonal:</p>
 * <ul>
 *   <li>Vive en la capa de Infraestructura</li>
 *   <li>Implementa un puerto definido en Aplicación</li>
 *   <li>Usa JPA para persistencia</li>
 *   <li>Mapea entre dominio y persistencia con MapStruct</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaCocheRepositorioAdaptador implements ReservaCocheRepositorioPuertoSalida {

    private final ReservaCocheRepositorioJPA repositorioJPA;
    private final ReservaCocheMapper mapper;

    @Override
    @Transactional
    public ReservaCoche guardar(ReservaCoche reserva) {
        log.debug("🔍 Guardando reserva de coche: {}", reserva.getId().valor());

        // Convertir de dominio a entidad JPA
        ReservaCocheEntidad entidad = mapper.aEntidad(reserva);

        // Persistir en BD
        ReservaCocheEntidad entidadGuardada = repositorioJPA.save(entidad);

        // Convertir de entidad JPA a dominio
        ReservaCoche reservaGuardada = mapper.aDominio(entidadGuardada);

        log.info("✅ Reserva de coche guardada: {}", reservaGuardada.getId().valor());
        return reservaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaCoche> buscarPorId(ReservaCocheId id) {
        log.debug("🔍 Buscando reserva por ID: {}", id.valor());

        return repositorioJPA.findById(id.valor())
                .map(entidad -> {
                    log.debug("✅ Reserva encontrada: {}", id.valor());
                    return mapper.aDominio(entidad);
                })
                .or(() -> {
                    log.debug("⚠️ Reserva no encontrada: {}", id.valor());
                    return Optional.empty();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaCoche> buscarPorReservaViajeId(String reservaViajeId) {
        log.debug("🔍 Buscando reserva por reservaViajeId: {}", reservaViajeId);

        return repositorioJPA.findByReservaViajeId(reservaViajeId)
                .map(entidad -> {
                    log.debug("✅ Reserva encontrada para reservaViajeId: {}", reservaViajeId);
                    return mapper.aDominio(entidad);
                })
                .or(() -> {
                    log.debug("⚠️ No hay reserva para reservaViajeId: {}", reservaViajeId);
                    return Optional.empty();
                });
    }

    @Override
    @Transactional
    public void eliminar(ReservaCocheId id) {
        log.info("🗑️ Eliminando reserva: {}", id.valor());

        if (!repositorioJPA.existsById(id.valor())) {
            log.warn("⚠️ Intento de eliminar reserva inexistente: {}", id.valor());
            throw new IllegalArgumentException("La reserva no existe: " + id.valor());
        }

        repositorioJPA.deleteById(id.valor());
        log.info("✅ Reserva eliminada: {}", id.valor());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorReservaViajeId(String reservaViajeId) {
        log.debug("🔍 Verificando existencia de reserva para reservaViajeId: {}", reservaViajeId);

        boolean existe = repositorioJPA.existsByReservaViajeId(reservaViajeId);

        log.debug("📊 Existe reserva para {}: {}", reservaViajeId, existe);
        return existe;
    }
}