package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.mapper;

import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.entidad.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PagoMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "pagoIdToString")
    @Mapping(target = "monto", source = "monto", qualifiedByName = "montoToDouble")
    @Mapping(target = "metodoPago", source = "metodoPago", qualifiedByName = "metodoPagoToEnum")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoPagoToEnum")
    PagoEntidad aEntidad(Pago pago);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToPagoId")
    @Mapping(target = "monto", source = "monto", qualifiedByName = "doubleToMonto")
    @Mapping(target = "metodoPago", source = "metodoPago", qualifiedByName = "enumToMetodoPago")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "enumToEstadoPago")
    Pago aDominio(PagoEntidad entidad);

    @Named("pagoIdToString")
    default String pagoIdToString(PagoId id) {
        return id != null ? id.valor() : null;
    }

    @Named("stringToPagoId")
    default PagoId stringToPagoId(String id) {
        return id != null ? new PagoId(id) : null;
    }

    @Named("montoToDouble")
    default Double montoToDouble(Monto monto) {
        return monto != null ? monto.valor() : null;
    }

    @Named("doubleToMonto")
    default Monto doubleToMonto(Double monto) {
        return monto != null ? new Monto(monto) : null;
    }

    @Named("metodoPagoToEnum")
    default MetodoPagoEnum metodoPagoToEnum(MetodoPago metodo) {
        if (metodo == null) return null;
        return MetodoPagoEnum.valueOf(metodo.name());
    }

    @Named("enumToMetodoPago")
    default MetodoPago enumToMetodoPago(MetodoPagoEnum metodo) {
        if (metodo == null) return null;
        return MetodoPago.valueOf(metodo.name());
    }

    @Named("estadoPagoToEnum")
    default EstadoPagoEnum estadoPagoToEnum(EstadoPago estado) {
        if (estado == null) return null;
        return EstadoPagoEnum.valueOf(estado.name());
    }

    @Named("enumToEstadoPago")
    default EstadoPago enumToEstadoPago(EstadoPagoEnum estado) {
        if (estado == null) return null;
        return EstadoPago.valueOf(estado.name());
    }
}
