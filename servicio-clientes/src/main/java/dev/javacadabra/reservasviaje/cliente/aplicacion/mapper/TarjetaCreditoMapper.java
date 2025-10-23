package dev.javacadabra.reservasviaje.cliente.aplicacion.mapper;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.TarjetaCreditoDTO;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.entidad.TarjetaCredito;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

/**
 * Mapper para convertir entre TarjetaCredito (entidad de dominio) y TarjetaCreditoDTO.
 *
 * <p>Sigue las mejores prácticas DDD para mappers con MapStruct.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TarjetaCreditoMapper {

    // ==================== DOMINIO → DTO ====================

    @Mapping(target = "tarjetaId", source = "tarjetaId", qualifiedByName = "tarjetaIdToString")
    @Mapping(target = "clienteId", source = "clienteId", qualifiedByName = "clienteIdToString")
    @Mapping(target = "numeroEnmascarado", expression = "java(tarjeta.obtenerNumeroEnmascarado())")
    @Mapping(target = "tipoTarjeta", source = "tipoTarjeta", qualifiedByName = "tipoToString")
    @Mapping(target = "estaExpirada", expression = "java(tarjeta.estaExpirada())")
    @Mapping(target = "esValida", expression = "java(tarjeta.esValida())")
    TarjetaCreditoDTO aDTO(TarjetaCredito tarjeta);

    List<TarjetaCreditoDTO> aDTOList(List<TarjetaCredito> tarjetas);

    // ==================== CONVERSIONES ====================

    @Named("tarjetaIdToString")
    default String tarjetaIdToString(TarjetaId tarjetaId) {
        return tarjetaId != null ? tarjetaId.valor() : null;
    }

    @Named("clienteIdToString")
    default String clienteIdToString(ClienteId clienteId) {
        return clienteId != null ? clienteId.valor() : null;
    }

    @Named("tipoToString")
    default String tipoToString(TipoTarjeta tipo) {
        return tipo != null ? tipo.name() : null;
    }
}