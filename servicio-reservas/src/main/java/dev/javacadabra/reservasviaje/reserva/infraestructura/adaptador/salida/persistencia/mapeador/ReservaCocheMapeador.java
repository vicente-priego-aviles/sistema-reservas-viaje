package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Currency;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ReservaCocheMapeador {

    @Mapping(target = "reservaId", source = "reservaId.valor")
    @Mapping(target = "empresaAlquiler", source = "datosCoche.empresaAlquiler")
    @Mapping(target = "modeloCoche", source = "datosCoche.modeloCoche")
    @Mapping(target = "categoriaCoche", source = "datosCoche.categoriaCoche")
    @Mapping(target = "ubicacionRecogida", source = "datosCoche.ubicacionRecogida")
    @Mapping(target = "ubicacionDevolucion", source = "datosCoche.ubicacionDevolucion")
    @Mapping(target = "fechaRecogida", source = "datosCoche.fechaRecogida")
    @Mapping(target = "fechaDevolucion", source = "datosCoche.fechaDevolucion")
    @Mapping(target = "precio", source = "precio.monto")
    @Mapping(target = "codigoMoneda", source = "precio.codigoMoneda")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "clienteId", source = "detalleReserva.clienteId")
    @Mapping(target = "observaciones", source = "detalleReserva.observaciones")
    @Mapping(target = "codigoConfirmacion", source = "detalleReserva.codigoConfirmacion")
    @Mapping(target = "motivoCancelacion", source = "detalleReserva.motivoCancelacion")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    @Mapping(target = "fechaModificacion", source = "fechaModificacion")
    ReservaCocheEntidad aEntidad(ReservaCoche reservaCoche);

    default ReservaCoche aDominio(ReservaCocheEntidad entidad) {
        return ReservaCoche.reconstruir(
                mapReservaId(entidad.getReservaId()),
                mapDatosCoche(entidad),
                mapPrecio(entidad.getPrecio(), entidad.getCodigoMoneda()),
                mapEstado(entidad.getEstado()),
                mapDetalleReserva(entidad),
                entidad.getFechaCreacion(),
                entidad.getFechaModificacion()
        );
    }

    @AfterMapping
    default void asignarId(@MappingTarget ReservaCoche reserva, ReservaCocheEntidad entidad) {
        if (entidad.getReservaId() != null) {
            reserva.asignarId(ReservaId.de(entidad.getReservaId()));
        }
    }

    // =================== AUXILIARES ===================
    default ReservaId mapReservaId(String reservaId) {
        return reservaId != null ? ReservaId.de(reservaId) : null;
    }

    default DatosCoche mapDatosCoche(ReservaCocheEntidad entidad) {
        return DatosCoche.builder()
                .empresaAlquiler(entidad.getEmpresaAlquiler())
                .modeloCoche(entidad.getModeloCoche())
                .categoriaCoche(entidad.getCategoriaCoche())
                .ubicacionRecogida(entidad.getUbicacionRecogida())
                .ubicacionDevolucion(entidad.getUbicacionDevolucion())
                .fechaRecogida(entidad.getFechaRecogida())
                .fechaDevolucion(entidad.getFechaDevolucion())
                .build();
    }

    default PrecioReserva mapPrecio(BigDecimal monto, String codigoMoneda) {
        if (monto == null) return null;
        Currency moneda = codigoMoneda != null ? Currency.getInstance(codigoMoneda) : Currency.getInstance("EUR");
        return PrecioReserva.de(monto, moneda);
    }

    default DetalleReserva mapDetalleReserva(ReservaCocheEntidad entidad) {
        return DetalleReserva.builder()
                .clienteId(entidad.getClienteId())
                .observaciones(entidad.getObservaciones())
                .codigoConfirmacion(entidad.getCodigoConfirmacion())
                .fechaCreacion(entidad.getFechaCreacion())
                .fechaModificacion(entidad.getFechaModificacion())
                .motivoCancelacion(entidad.getMotivoCancelacion())
                .build();
    }

    default EstadoReserva mapEstado(String estado) {
        return estado != null ? EstadoReserva.valueOf(estado) : null;
    }
}
