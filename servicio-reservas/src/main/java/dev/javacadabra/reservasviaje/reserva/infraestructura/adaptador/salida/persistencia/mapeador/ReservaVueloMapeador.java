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

/**
 * Mapeador entre ReservaVuelo (dominio) y ReservaVueloEntidad (JPA).
 * MapStruct genera automáticamente la implementación.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ReservaVueloMapeador {

    /**
     * Convierte el agregado de dominio a entidad JPA.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservaId", source = "reservaId")
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

    /**
     * Convierte la entidad JPA a agregado de dominio.
     */
    @Mapping(target = "reservaId", expression = "java(mapReservaId(entidad.getReservaId()))")
    @Mapping(target = "datosVuelo", expression = "java(mapDatosVuelo(entidad))")
    @Mapping(target = "precio", expression = "java(mapPrecio(entidad.getPrecio(), entidad.getCodigoMoneda()))")
    @Mapping(target = "estado", expression = "java(mapEstado(entidad.getEstado()))")
    @Mapping(target = "detalleReserva", expression = "java(mapDetalleReserva(entidad))")
    @Mapping(target = "pasajeros", source = "pasajeros")
    ReservaVuelo aDominio(ReservaVueloEntidad entidad);

    /**
     * Convierte Pasajero (dominio) a PasajeroEntidad (JPA).
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "reservaVuelo", ignore = true)
    PasajeroEntidad pasajeroAEntidad(Pasajero pasajero);

    /**
     * Convierte PasajeroEntidad (JPA) a Pasajero (dominio).
     */
    @Mapping(target = "id", source = "id")
    Pasajero pasajeroADominio(PasajeroEntidad entidad);

    /**
     * Convierte lista de Pasajeros a lista de PasajeroEntidades.
     */
    List<PasajeroEntidad> pasajerosAEntidades(List<Pasajero> pasajeros);

    /**
     * Convierte lista de PasajeroEntidades a lista de Pasajeros.
     */
    List<Pasajero> pasajerosADominio(List<PasajeroEntidad> entidades);

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
        Currency moneda = codigoMoneda != null
                ? Currency.getInstance(codigoMoneda)
                : Currency.getInstance("EUR");
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
}
