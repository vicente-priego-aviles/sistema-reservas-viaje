package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Mapeador entre ReservaCoche (dominio) y ReservaCocheEntidad (JPA).
 * MapStruct genera automáticamente la implementación.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ReservaCocheMapeador {

    /**
     * Convierte el agregado de dominio a entidad JPA.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservaId", source = "reservaId")
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

    /**
     * Convierte la entidad JPA a agregado de dominio.
     */
    @Mapping(target = "reservaId", expression = "java(mapReservaId(entidad.getReservaId()))")
    @Mapping(target = "datosCoche", expression = "java(mapDatosCoche(entidad))")
    @Mapping(target = "precio", expression = "java(mapPrecio(entidad.getPrecio(), entidad.getCodigoMoneda()))")
    @Mapping(target = "estado", expression = "java(mapEstado(entidad.getEstado()))")
    @Mapping(target = "detalleReserva", expression = "java(mapDetalleReserva(entidad))")
    ReservaCoche aDominio(ReservaCocheEntidad entidad);

    // ========== Métodos de mapeo de Value Objects ==========

    default String mapReservaIdToString(ReservaId reservaId) {
        return reservaId != null ? reservaId.getValor() : null;
    }

    default ReservaId mapReservaId(String reservaId) {
        return reservaId != null ? ReservaId.de(reservaId) : null;
    }

    default String mapEstadoToString(EstadoReserva estado) {
        return estado != null ? estado.name() : null;
    }

    default EstadoReserva mapEstado(String estado) {
        return estado != null ? EstadoReserva.valueOf(estado) : null;
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
        Currency moneda = codigoMoneda != null
                ? Currency.getInstance(codigoMoneda)
                : Currency.getInstance("EUR");
        return PrecioReserva.de(monto, moneda);
    }

    default DetalleReserva mapDetalleReserva(ReservaCocheEntidad entidad) {
        return DetalleReserva.builder()
                .id(entidad.getId())
                .clienteId(entidad.getClienteId())
                .observaciones(entidad.getObservaciones())
                .codigoConfirmacion(entidad.getCodigoConfirmacion())
                .fechaCreacion(entidad.getFechaCreacion())
                .fechaModificacion(entidad.getFechaModificacion())
                .motivoCancelacion(entidad.getMotivoCancelacion())
                .build();
    }
}
