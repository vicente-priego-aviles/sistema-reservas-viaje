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
 * Usa ClienteMapper para compartir métodos comunes y evitar duplicación.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ClienteMapper.class}  // ✅ Compartir métodos comunes con ClienteMapper
)
public interface TarjetaCreditoMapper {

    // ==================== DOMINIO → DTO ====================

    @Mapping(target = "tarjetaId", source = "tarjetaId", qualifiedByName = "tarjetaIdToString")
    @Mapping(target = "numeroEnmascarado", expression = "java(tarjeta.obtenerNumeroEnmascarado())")
    @Mapping(target = "ultimosDigitos", expression = "java(extraerUltimosDigitos(tarjeta.obtenerNumeroEnmascarado()))")
    @Mapping(target = "tipoTarjeta", source = "tipoTarjeta", qualifiedByName = "tipoToString")
    @Mapping(target = "nombreTipoTarjeta", expression = "java(tarjeta.getTipoTarjeta().getNombre())")
    @Mapping(target = "validada", source = "validada")
    @Mapping(target = "estaExpirada", expression = "java(tarjeta.estaExpirada())")
    @Mapping(target = "esValida", expression = "java(tarjeta.esValida())")
    @Mapping(target = "expiraPronto", expression = "java(tarjeta.expiraPronto())")
    @Mapping(target = "mesesHastaExpiracion", expression = "java(tarjeta.mesesHastaExpiracion())")
    @Mapping(target = "motivoRechazo", source = "motivoRechazo")
    TarjetaCreditoDTO aDTO(TarjetaCredito tarjeta);

    /**
     * Convierte una lista de tarjetas de crédito a DTOs.
     *
     * @param tarjetas lista de tarjetas del dominio
     * @return lista de DTOs
     */
    List<TarjetaCreditoDTO> aDTOList(List<TarjetaCredito> tarjetas);

    // ==================== CONVERSIONES ====================

    /**
     * Convierte TarjetaId a String.
     *
     * @param tarjetaId identificador de tarjeta
     * @return valor del ID como String
     */
    @Named("tarjetaIdToString")
    default String tarjetaIdToString(TarjetaId tarjetaId) {
        return tarjetaId != null ? tarjetaId.valor() : null;
    }

    /**
     * Convierte TipoTarjeta enum a String.
     *
     * @param tipo tipo de tarjeta
     * @return nombre del enum
     */
    @Named("tipoToString")
    default String tipoToString(TipoTarjeta tipo) {
        return tipo != null ? tipo.name() : null;
    }

    /**
     * Extrae los últimos 4 dígitos del número enmascarado.
     *
     * @param numeroEnmascarado número de tarjeta enmascarado
     * @return últimos 4 dígitos
     */
    @Named("extraerUltimosDigitosTarjeta")
    default String extraerUltimosDigitos(String numeroEnmascarado) {
        if (numeroEnmascarado == null || numeroEnmascarado.length() < 4) {
            return numeroEnmascarado;
        }
        return numeroEnmascarado.substring(Math.max(0, numeroEnmascarado.length() - 4));
    }
}