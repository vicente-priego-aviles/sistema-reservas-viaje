package dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.mapper;

import dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.CategoriaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.EstadoReservaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.ReservaCocheId;
import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.entidad.CategoriaCocheEnum;
import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.entidad.EstadoReservaCocheEnum;
import dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.entidad.ReservaCocheEntidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservaCocheMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "reservaCocheIdToString")
    @Mapping(target = "fechaInicio", source = "fechaRecogida")
    @Mapping(target = "fechaFin", source = "fechaDevolucion")
    @Mapping(target = "modeloCoche", source = "modelo")
    @Mapping(target = "lugarRecogida", source = "puntoRecogida")
    @Mapping(target = "lugarDevolucion", source = "puntoDevolucion")
    @Mapping(target = "categoriaCoche", source = "categoria", qualifiedByName = "categoriaToEnum")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToEnum")
    @Mapping(target = "marcaCoche", ignore = true)
    @Mapping(target = "precioPorDia", ignore = true)
    @Mapping(target = "precioTotal", ignore = true)
    @Mapping(target = "observaciones", ignore = true)
    ReservaCocheEntidad aEntidad(ReservaCoche reserva);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToReservaCocheId")
    @Mapping(target = "fechaRecogida", source = "fechaInicio")
    @Mapping(target = "fechaDevolucion", source = "fechaFin")
    @Mapping(target = "modelo", source = "modeloCoche")
    @Mapping(target = "puntoRecogida", source = "lugarRecogida")
    @Mapping(target = "puntoDevolucion", source = "lugarDevolucion")
    @Mapping(target = "categoria", source = "categoriaCoche", qualifiedByName = "enumToCategoria")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "enumToEstado")
    ReservaCoche aDominio(ReservaCocheEntidad entidad);

    @Named("reservaCocheIdToString")
    default String reservaCocheIdToString(ReservaCocheId id) {
        return id != null ? id.valor() : null;
    }

    @Named("stringToReservaCocheId")
    default ReservaCocheId stringToReservaCocheId(String id) {
        return id != null ? new ReservaCocheId(id) : null;
    }

    @Named("estadoToEnum")
    default EstadoReservaCocheEnum estadoToEnum(EstadoReservaCoche estado) {
        return estado != null ? EstadoReservaCocheEnum.valueOf(estado.name()) : null;
    }

    @Named("enumToEstado")
    default EstadoReservaCoche enumToEstado(EstadoReservaCocheEnum estado) {
        return estado != null ? EstadoReservaCoche.valueOf(estado.name()) : null;
    }

    @Named("categoriaToEnum")
    default CategoriaCocheEnum categoriaToEnum(CategoriaCoche categoria) {
        return categoria != null ? CategoriaCocheEnum.valueOf(categoria.name()) : null;
    }

    @Named("enumToCategoria")
    default CategoriaCoche enumToCategoria(CategoriaCocheEnum categoria) {
        return categoria != null ? CategoriaCoche.valueOf(categoria.name()) : null;
    }
}
