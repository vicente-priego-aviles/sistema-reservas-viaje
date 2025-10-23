package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.mapeador;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.Pasajero;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.PasajeroEntidad;
import dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad.ReservaVueloEntidad;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ReservaVueloMapeador {

    // =================== DOMINIO → ENTIDAD ===================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservaId", source = "reservaId.valor")
    @Mapping(target = "numeroVuelo", source = "datosVuelo.numeroVuelo")
    @Mapping(target = "aerolinea", source = "datosVuelo.aerolinea")
    @Mapping(target = "origen", source = "datosVuelo.origen")
    @Mapping(target = "destino", source = "datosVuelo.destino")
    @Mapping(target = "fechaSalida", source = "datosVuelo.fechaSalida")
    @Mapping(target = "fechaLlegada", source = "datosVuelo.fechaLlegada")
    @Mapping(target = "clase", source = "datosVuelo.clase")
    @Mapping(target = "numeroPasajeros", source = "datosVuelo.numeroPasajeros")
    @Mapping(target = "precio", source = "precio.monto")
    @Mapping(target = "codigoMoneda", source = "precio.codigoMoneda")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "clienteId", source = "detalleReserva.clienteId")
    @Mapping(target = "observaciones", source = "detalleReserva.observaciones")
    @Mapping(target = "codigoConfirmacion", source = "detalleReserva.codigoConfirmacion")
    @Mapping(target = "motivoCancelacion", source = "detalleReserva.motivoCancelacion")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    @Mapping(target = "fechaModificacion", source = "fechaModificacion")
    @Mapping(target = "pasajeros", source = "pasajeros")
    ReservaVueloEntidad aEntidad(ReservaVuelo reservaVuelo);

    // =================== ENTIDAD → DOMINIO ===================
    default ReservaVuelo aDominio(ReservaVueloEntidad entidad) {
        return ReservaVuelo.reconstruir(
                mapReservaId(entidad.getReservaId()),
                mapDatosVuelo(entidad),
                pasajerosADominio(entidad.getPasajeros()),
                mapPrecio(entidad.getPrecio(), entidad.getCodigoMoneda()),
                mapEstado(entidad.getEstado()),
                mapDetalleReserva(entidad),
                entidad.getFechaCreacion(),
                entidad.getFechaModificacion()
        );
    }

    @AfterMapping
    default void asignarId(@MappingTarget ReservaVuelo reserva, ReservaVueloEntidad entidad) {
        if (entidad.getReservaId() != null) {
            reserva.asignarId(ReservaId.de(entidad.getReservaId()));
        }
    }

    // =================== PASAJEROS ===================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "reservaVuelo", ignore = true)
    PasajeroEntidad pasajeroAEntidad(Pasajero pasajero);

    @Mapping(target = "id", source = "id")
    Pasajero pasajeroADominio(PasajeroEntidad entidad);

    List<PasajeroEntidad> pasajerosAEntidades(List<Pasajero> pasajeros);
    List<Pasajero> pasajerosADominio(List<PasajeroEntidad> entidades);

    // =================== AUXILIARES ===================
    default ReservaId mapReservaId(String reservaId) {
        return reservaId != null ? ReservaId.de(reservaId) : null;
    }

    default DatosVuelo mapDatosVuelo(ReservaVueloEntidad entidad) {
        return DatosVuelo.builder()
                .numeroVuelo(entidad.getNumeroVuelo())
                .aerolinea(entidad.getAerolinea())
                .origen(entidad.getOrigen())
                .destino(entidad.getDestino())
                .fechaSalida(entidad.getFechaSalida())
                .fechaLlegada(entidad.getFechaLlegada())
                .clase(entidad.getClase())
                .numeroPasajeros(entidad.getNumeroPasajeros())
                .build();
    }

    default PrecioReserva mapPrecio(BigDecimal monto, String codigoMoneda) {
        if (monto == null) return null;
        Currency moneda = codigoMoneda != null ? Currency.getInstance(codigoMoneda) : Currency.getInstance("EUR");
        return PrecioReserva.de(monto, moneda);
    }

    default DetalleReserva mapDetalleReserva(ReservaVueloEntidad entidad) {
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

    default EstadoReserva mapEstado(String estado) {
        return estado != null ? EstadoReserva.valueOf(estado) : null;
    }
}
