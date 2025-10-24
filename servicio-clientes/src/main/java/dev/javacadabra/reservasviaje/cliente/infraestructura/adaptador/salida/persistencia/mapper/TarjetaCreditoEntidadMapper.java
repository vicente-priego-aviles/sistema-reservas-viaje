package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.mapper;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.entidad.TarjetaCredito;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.TarjetaCreditoEntidad;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Mapper para convertir entre TarjetaCredito (dominio) y TarjetaCreditoEntidad (JPA).
 *
 * <p>Sigue las mejores prácticas DDD: usa métodos default para reconstruir
 * el dominio inmutable desde la entidad JPA.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TarjetaCreditoEntidadMapper {

    // ==================== DOMINIO → JPA ====================

    @Mapping(target = "id", source = "tarjetaId", qualifiedByName = "tarjetaIdToString")
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "numeroEncriptado", source = "numeroTarjeta.valorEncriptado")
    @Mapping(target = "ultimosDigitos", expression = "java(extraerUltimosDigitos(tarjeta.obtenerNumeroEnmascarado()))")
    @Mapping(target = "anioExpiracion", source = "fechaExpiracion.year")
    @Mapping(target = "mesExpiracion", source = "fechaExpiracion.monthValue")
    @Mapping(target = "tipoTarjeta", source = "tipoTarjeta", qualifiedByName = "tipoToEnum")
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    TarjetaCreditoEntidad aEntidad(TarjetaCredito tarjeta);

    List<TarjetaCreditoEntidad> aEntidadList(List<TarjetaCredito> tarjetas);

    // ==================== JPA → DOMINIO ====================

    /**
     * Reconstruye TarjetaCredito desde entidad JPA.
     * Usa método default para evitar que MapStruct intente usar setters.
     */
    default TarjetaCredito aDominio(TarjetaCreditoEntidad entidad) {
        if (entidad == null) {
            return null;
        }

        return TarjetaCredito.reconstruir(
                stringToTarjetaId(entidad.getId()),
                stringToClienteId(entidad.getCliente().getId()),
                stringToNumeroTarjeta(entidad.getNumeroEncriptado()),
                YearMonth.of(entidad.getAnioExpiracion(), entidad.getMesExpiracion()),
                enumToTipo(entidad.getTipoTarjeta()),
                (entidad.getFechaCreacion() != null ? entidad.getFechaCreacion() : LocalDateTime.now()),
                entidad.getValidada() != null ? entidad.getValidada() : false,
                entidad.getMotivoRechazo()
        );
    }

    default List<TarjetaCredito> aDominioList(List<TarjetaCreditoEntidad> entidades) {
        if (entidades == null) {
            return null;
        }
        return entidades.stream()
                .map(this::aDominio)
                .toList();
    }

    // ==================== CONVERSIONES ====================

    @Named("tarjetaIdToString")
    default String tarjetaIdToString(TarjetaId tarjetaId) {
        return tarjetaId != null ? tarjetaId.valor() : null;
    }

    default TarjetaId stringToTarjetaId(String id) {
        return id != null ? TarjetaId.de(id) : null;
    }

    default ClienteId stringToClienteId(String id) {
        return id != null ? ClienteId.de(id) : null;
    }

    default NumeroTarjeta stringToNumeroTarjeta(String numeroEncriptado) {
        return numeroEncriptado != null ? NumeroTarjeta.reconstruido(numeroEncriptado) : null;
    }

    @Named("tipoToEnum")
    default TarjetaCreditoEntidad.TipoTarjetaEnum tipoToEnum(TipoTarjeta tipo) {
        if (tipo == null) return null;
        return TarjetaCreditoEntidad.TipoTarjetaEnum.valueOf(tipo.name());
    }

    default TipoTarjeta enumToTipo(TarjetaCreditoEntidad.TipoTarjetaEnum tipoEnum) {
        if (tipoEnum == null) return null;
        return TipoTarjeta.valueOf(tipoEnum.name());
    }

    default String extraerUltimosDigitos(String numeroEnmascarado) {
        if (numeroEnmascarado == null || numeroEnmascarado.length() < 4) {
            return "****";
        }
        String soloDigitos = numeroEnmascarado.replaceAll("[^0-9]", "");
        if (soloDigitos.length() < 4) {
            return "****";
        }
        return soloDigitos.substring(soloDigitos.length() - 4);
    }
}