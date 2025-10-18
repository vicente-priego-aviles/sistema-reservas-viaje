package dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.adaptador;

import dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.salida.ReservaVueloRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.AsientoNumero;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.EstadoReservaVuelo;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.ReservaVueloId;
import dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.entidad.EstadoReservaVueloEnum;
import dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.entidad.ReservaVueloJpaEntity;
import dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.repositorio.ReservaVueloRepositorioJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaVueloRepositorioAdaptador implements ReservaVueloRepositorioPuertoSalida {

    private final ReservaVueloRepositorioJpa repositorioJpa;
    // Mapper para convertir entre dominio y JPA (implementar con MapStruct)

    @Override
    public ReservaVuelo guardar(ReservaVuelo reserva) {
        log.debug("💾 Guardando reserva de vuelo: {}", reserva.getId().valor());

        ReservaVueloJpaEntity entity = toEntity(reserva);
        ReservaVueloJpaEntity guardado = repositorioJpa.save(entity);

        return toDomain(guardado);
    }

    @Override
    public Optional<ReservaVuelo> buscarPorId(ReservaVueloId id) {
        return repositorioJpa.findById(id.valor()).map(this::toDomain);
    }

    @Override
    public Optional<ReservaVuelo> buscarPorReservaViajeId(String reservaViajeId) {
        return repositorioJpa.findByReservaViajeId(reservaViajeId).map(this::toDomain);
    }

    @Override
    public void eliminar(ReservaVueloId id) {
        repositorioJpa.deleteById(id.valor());
    }

    // Métodos de mapeo (simplificados - usar MapStruct en producción)
    private ReservaVueloJpaEntity toEntity(ReservaVuelo reserva) {
        return ReservaVueloJpaEntity.builder()
                .id(reserva.getId().valor())
                .reservaViajeId(reserva.getReservaViajeId())
                .clienteId(reserva.getClienteId())
                .origen(reserva.getOrigen())
                .destino(reserva.getDestino())
                .fechaSalida(reserva.getFechaSalida())
                .numeroVuelo(reserva.getNumeroVuelo())
                .aerolinea(reserva.getAerolinea())
                .asiento(reserva.getAsiento() != null ? reserva.getAsiento().valor() : null)
                .numeroReserva(reserva.getNumeroReserva())
                .estado(EstadoReservaVueloEnum.valueOf(reserva.getEstado().name()))
                .fechaReserva(reserva.getFechaReserva())
                .fechaCancelacion(reserva.getFechaCancelacion())
                .build();
    }

    private ReservaVuelo toDomain(ReservaVueloJpaEntity entity) {
        return ReservaVuelo.builder()
                .id(new ReservaVueloId(entity.getId()))
                .reservaViajeId(entity.getReservaViajeId())
                .clienteId(entity.getClienteId())
                .origen(entity.getOrigen())
                .destino(entity.getDestino())
                .fechaSalida(entity.getFechaSalida())
                .numeroVuelo(entity.getNumeroVuelo())
                .aerolinea(entity.getAerolinea())
                .asiento(entity.getAsiento() != null ? new AsientoNumero(entity.getAsiento()) : null)
                .numeroReserva(entity.getNumeroReserva())
                .estado(EstadoReservaVuelo.valueOf(entity.getEstado().name()))
                .fechaReserva(entity.getFechaReserva())
                .fechaCancelacion(entity.getFechaCancelacion())
                .build();
    }
}
