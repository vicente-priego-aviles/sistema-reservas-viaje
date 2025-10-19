package dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.mapper;

import dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.ReservaCocheId;
import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservaCocheMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "reservaCocheIdToString")
    ReservaCocheEntidad aEntidad(ReservaCoche reserva);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToReservaCocheId")
    ReservaCoche aDominio(ReservaCocheEntidad entidad);

    @Named("reservaCocheIdToString")
    default String reservaCocheIdToString(ReservaCocheId id) {
        return id != null ? id.valor() : null;
    }

    @Named("stringToReservaCocheId")
    default ReservaCocheId stringToReservaCocheId(String id) {
        return id != null ? new ReservaCocheId(id) : null;
    }
}
