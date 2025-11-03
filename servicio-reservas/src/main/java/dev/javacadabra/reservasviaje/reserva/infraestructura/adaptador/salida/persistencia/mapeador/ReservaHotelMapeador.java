package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaHotelEntidad;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Currency;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ReservaHotelMapeador {

    @Mapping(target = "reservaId", source = "reservaId.valor")
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

    default ReservaHotel aDominio(ReservaHotelEntidad entidad) {
        return ReservaHotel.reconstruir(
                mapReservaId(entidad.getReservaId()),
                mapDatosHotel(entidad),
                mapPrecio(entidad.getPrecio(), entidad.getCodigoMoneda()),
                mapEstado(entidad.getEstado()),
                mapDetalleReserva(entidad),
                entidad.getFechaCreacion(),
                entidad.getFechaModificacion()
        );
    }

    @AfterMapping
    default void asignarId(@MappingTarget ReservaHotel reserva, ReservaHotelEntidad entidad) {
        if (entidad.getReservaId() != null) {
            reserva.asignarId(ReservaId.de(entidad.getReservaId()));
        }
    }

    // =================== AUXILIARES ===================
    default ReservaId mapReservaId(String reservaId) {
        return reservaId != null ? ReservaId.de(reservaId) : null;
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
        Currency moneda = codigoMoneda != null ? Currency.getInstance(codigoMoneda) : Currency.getInstance("EUR");
        return PrecioReserva.de(monto, moneda);
    }

    default DetalleReserva mapDetalleReserva(ReservaHotelEntidad entidad) {
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
