package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaHotelEntidad;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Mapeador entre ReservaHotel (dominio) y ReservaHotelEntidad (JPA).
 * MapStruct genera automáticamente la implementación.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ReservaHotelMapeador {

    /**
     * Convierte el agregado de dominio a entidad JPA.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservaId", source = "reservaId")
    @Mapping(target = "nombreHotel", source = "datosHotel.nombreHotel")
    @Mapping(target = "ciudad", source = "datosHotel.ciudad")
    @Mapping(target = "direccion", source = "datosHotel.direccion")
    @Mapping(target = "fechaEntrada", source = "datosHotel.fechaEntrada")
    @Mapping(target = "fechaSalida", source = "datosHotel.fechaSalida")
    @Mapping(target = "tipoHabitacion", source = "datosHotel.tipoHabitacion")
    @Mapping(target = "numeroHabitaciones", source = "datosHotel.numeroHabitaciones")
    @Mapping(target = "numeroHuespedes", source = "datosHotel.numeroHuespedes")
    @Mapping(target = "precio", source = "precio.monto")
    @Mapping(target = "codigoMoneda", source = "precio.codigoMoneda")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "clienteId", source = "detalleReserva.clienteId")
    @Mapping(target = "observaciones", source = "detalleReserva.observaciones")
    @Mapping(target = "codigoConfirmacion", source = "detalleReserva.codigoConfirmacion")
    @Mapping(target = "motivoCancelacion", source = "detalleReserva.motivoCancelacion")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    @Mapping(target = "fechaModificacion", source = "fechaModificacion")
    ReservaHotelEntidad aEntidad(ReservaHotel reservaHotel);

    /**
     * Convierte la entidad JPA a agregado de dominio.
     */
    @Mapping(target = "reservaId", expression = "java(mapReservaId(entidad.getReservaId()))")
    @Mapping(target = "datosHotel", expression = "java(mapDatosHotel(entidad))")
    @Mapping(target = "precio", expression = "java(mapPrecio(entidad.getPrecio(), entidad.getCodigoMoneda()))")
    @Mapping(target = "estado", expression = "java(mapEstado(entidad.getEstado()))")
    @Mapping(target = "detalleReserva", expression = "java(mapDetalleReserva(entidad))")
    ReservaHotel aDominio(ReservaHotelEntidad entidad);

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

    default DatosHotel mapDatosHotel(ReservaHotelEntidad entidad) {
        return DatosHotel.builder()
                .nombreHotel(entidad.getNombreHotel())
                .ciudad(entidad.getCiudad())
                .direccion(entidad.getDireccion())
                .fechaEntrada(entidad.getFechaEntrada())
                .fechaSalida(entidad.getFechaSalida())
                .tipoHabitacion(entidad.getTipoHabitacion())
                .numeroHabitaciones(entidad.getNumeroHabitaciones())
                .numeroHuespedes(entidad.getNumeroHuespedes())
                .build();
    }

    default PrecioReserva mapPrecio(BigDecimal monto, String codigoMoneda) {
        if (monto == null) return null;
        Currency moneda = codigoMoneda != null
                ? Currency.getInstance(codigoMoneda)
                : Currency.getInstance("EUR");
        return PrecioReserva.de(monto, moneda);
    }

    default DetalleReserva mapDetalleReserva(ReservaHotelEntidad entidad) {
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
