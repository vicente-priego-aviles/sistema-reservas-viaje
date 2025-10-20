package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.adaptador;

import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.salida.PagoRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.PagoId;
import dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.entidad.PagoEntidad;
import dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.mapper.PagoMapper;
import dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.repositorio.PagoRepositorioJPA;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto de repositorio.
 *
 * <p>Este adaptador traduce entre el modelo de dominio (Pago)
 * y el modelo de persistencia (PagoEntidad).</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PagoRepositorioAdaptador implements PagoRepositorioPuertoSalida {

    private final PagoRepositorioJPA repositorioJPA;
    private final PagoMapper mapper;

    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        log.debug("🔍 Guardando pago: {}", pago.getId().valor());

        PagoEntidad entidad = mapper.aEntidad(pago);
        PagoEntidad entidadGuardada = repositorioJPA.save(entidad);
        Pago pagoGuardado = mapper.aDominio(entidadGuardada);

        log.info("✅ Pago guardado: {} - Estado: {}",
                pagoGuardado.getId().valor(), pagoGuardado.getEstado());
        return pagoGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> buscarPorId(PagoId id) {
        log.debug("🔍 Buscando pago por ID: {}", id.valor());

        return repositorioJPA.findById(id.valor())
                .map(entidad -> {
                    log.debug("✅ Pago encontrado: {}", id.valor());
                    return mapper.aDominio(entidad);
                })
                .or(() -> {
                    log.debug("⚠️ Pago no encontrado: {}", id.valor());
                    return Optional.empty();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> buscarPorReservaViajeId(String reservaViajeId) {
        log.debug("🔍 Buscando pago por reservaViajeId: {}", reservaViajeId);

        return repositorioJPA.findByReservaViajeId(reservaViajeId)
                .map(entidad -> {
                    log.debug("✅ Pago encontrado para reservaViajeId: {}", reservaViajeId);
                    return mapper.aDominio(entidad);
                })
                .or(() -> {
                    log.debug("⚠️ No hay pago para reservaViajeId: {}", reservaViajeId);
                    return Optional.empty();
                });
    }

    @Override
    @Transactional
    public void eliminar(PagoId id) {
        log.info("🗑️ Eliminando pago: {}", id.valor());

        if (!repositorioJPA.existsById(id.valor())) {
            log.warn("⚠️ Intento de eliminar pago inexistente: {}", id.valor());
            throw new IllegalArgumentException("El pago no existe: " + id.valor());
        }

        repositorioJPA.deleteById(id.valor());
        log.info("✅ Pago eliminado: {}", id.valor());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorReservaViajeId(String reservaViajeId) {
        log.debug("🔍 Verificando existencia de pago para reservaViajeId: {}", reservaViajeId);

        boolean existe = repositorioJPA.existsByReservaViajeId(reservaViajeId);

        log.debug("📊 Existe pago para {}: {}", reservaViajeId, existe);
        return existe;
    }
}
