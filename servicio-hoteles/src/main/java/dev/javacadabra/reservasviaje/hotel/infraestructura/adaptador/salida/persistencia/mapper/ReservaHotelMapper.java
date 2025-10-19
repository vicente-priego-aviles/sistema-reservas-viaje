package dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.salida.persistencia.mapper;

import dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.EstadoReservaHotel;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.HabitacionNumero;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.ReservaHotelId;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.TipoHabitacion;
import dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.salida.persistencia.entidad.ReservaHotelEntidad;
import dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.salida.persistencia.entidad.EstadoReservaHotelEnum;
import dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.salida.persistencia.entidad.TipoHabitacionEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservaHotelMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "reservaHotelIdToString")
    @Mapping(target = "numeroHabitacion", source = "numeroHabitacion", qualifiedByName = "habitacionNumeroToString")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToEnum")
    @Mapping(target = "tipoHabitacion", source = "tipoHabitacion", qualifiedByName = "tipoHabitacionToEnum")
    ReservaHotelEntidad aEntidad(ReservaHotel reserva);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToReservaHotelId")
    @Mapping(target = "numeroHabitacion", source = "numeroHabitacion", qualifiedByName = "stringToHabitacionNumero")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "enumToEstado")
    @Mapping(target = "tipoHabitacion", source = "tipoHabitacion", qualifiedByName = "enumToTipoHabitacion")
    ReservaHotel aDominio(ReservaHotelEntidad entidad);

    @Named("reservaHotelIdToString")
    default String reservaHotelIdToString(ReservaHotelId id) {
        return id != null ? id.valor() : null;
    }

    @Named("stringToReservaHotelId")
    default ReservaHotelId stringToReservaHotelId(String id) {
        return id != null ? new ReservaHotelId(id) : null;
    }

    @Named("habitacionNumeroToString")
    default String habitacionNumeroToString(HabitacionNumero numero) {
        return numero != null ? numero.valor() : null;
    }

    @Named("stringToHabitacionNumero")
    default HabitacionNumero stringToHabitacionNumero(String numero) {
        return numero != null ? new HabitacionNumero(numero) : null;
    }

    @Named("estadoToEnum")
    default EstadoReservaHotelEnum estadoToEnum(EstadoReservaHotel estado) {
        if (estado == null) return null;
        return EstadoReservaHotelEnum.valueOf(estado.name());
    }

    @Named("enumToEstado")
    default EstadoReservaHotel enumToEstado(EstadoReservaHotelEnum estado) {
        if (estado == null) return null;
        return EstadoReservaHotel.valueOf(estado.name());
    }

    @Named("tipoHabitacionToEnum")
    default TipoHabitacionEnum tipoHabitacionToEnum(TipoHabitacion tipo) {
        if (tipo == null) return null;
        return TipoHabitacionEnum.valueOf(tipo.name());
    }

    @Named("enumToTipoHabitacion")
    default TipoHabitacion enumToTipoHabitacion(TipoHabitacionEnum tipo) {
        if (tipo == null) return null;
        return TipoHabitacion.valueOf(tipo.name());
    }
}
