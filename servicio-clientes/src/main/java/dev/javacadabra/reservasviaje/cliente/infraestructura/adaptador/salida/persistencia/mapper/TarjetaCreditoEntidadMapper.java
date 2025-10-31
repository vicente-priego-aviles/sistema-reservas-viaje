package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.mapper;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.entidad.TarjetaCredito;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.TarjetaCreditoEntidad;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Mapper para convertir entre TarjetaCredito (dominio) y TarjetaCreditoEntidad (JPA).
 *
 * <p>Sigue las mejores prácticas DDD: usa métodos default para reconstruir
 * el dominio inmutable desde la entidad JPA.
 *
 * <p><strong>IMPORTANTE:</strong> Este mapper usa métodos default en lugar de
 * @Mapping automático para evitar que MapStruct confunda los campos encriptados.
 *
 * @author javacadabra
 * @version 1.0.1 - Corregido bug de encriptación
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TarjetaCreditoEntidadMapper {

    // ==================== DOMINIO → JPA ====================

    /**
     * Convierte TarjetaCredito (dominio) a TarjetaCreditoEntidad (JPA).
     *
     * <p><strong>✅ CORREGIDO:</strong> Usa método default manual para evitar
     * que MapStruct aplique extraerUltimosDigitos() al campo numeroEncriptado.
     *
     * <p><strong>Bug anterior:</strong> MapStruct generaba código que aplicaba
     * extraerUltimosDigitos() tanto a numeroEncriptado como a ultimosDigitos,
     * causando que se guardara "****" en lugar del Base64 encriptado.
     */
    default TarjetaCreditoEntidad aEntidad(TarjetaCredito tarjeta) {
        if (tarjeta == null) {
            return null;
        }

        TarjetaCreditoEntidad entidad = new TarjetaCreditoEntidad();

        // ✅ ID de la tarjeta
        entidad.setId(tarjetaIdToString(tarjeta.getTarjetaId()));

        // ✅ CRÍTICO: Número encriptado en Base64 (SIN procesar)
        entidad.setNumeroEncriptado(tarjeta.getNumeroTarjeta().getValorEncriptado());

        // ✅ CRÍTICO: Últimos dígitos extraídos del número enmascarado
        entidad.setUltimosDigitos(extraerUltimosDigitos(tarjeta.obtenerNumeroEnmascarado()));

        // Fecha de expiración
        entidad.setAnioExpiracion(tarjeta.getFechaExpiracion().getYear());
        entidad.setMesExpiracion(tarjeta.getFechaExpiracion().getMonthValue());

        // Tipo de tarjeta
        entidad.setTipoTarjeta(tipoToEnum(tarjeta.getTipoTarjeta()));

        // Estado de validación
        entidad.setValidada(tarjeta.isValidada());
        entidad.setMotivoRechazo(tarjeta.getMotivoRechazo());

        // Cliente se establece desde el padre (bidireccionalidad)

        return entidad;
    }

    default List<TarjetaCreditoEntidad> aEntidadList(List<TarjetaCredito> tarjetas) {
        if (tarjetas == null) {
            return null;
        }
        return tarjetas.stream()
                .map(this::aEntidad)
                .toList();
    }

    // ==================== JPA → DOMINIO ====================

    /**
     * Reconstruye TarjetaCredito desde entidad JPA.
     *
     * <p>El CVV no se persiste según PCI DSS, por lo que se reconstruye como null.
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
                // ✅ CORREGIDO: Convertir LocalDateTime a LocalDate
                entidad.getFechaCreacion() != null
                        ? entidad.getFechaCreacion()
                        : LocalDateTime.now(),
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

    /**
     * Convierte el número encriptado (Base64) a NumeroTarjeta.
     *
     * <p><strong>Validación:</strong> Verifica que el valor sea Base64 válido
     * antes de intentar reconstruir el Value Object.
     *
     * @param numeroEncriptado número encriptado en Base64
     * @return NumeroTarjeta reconstruido
     * @throws IllegalArgumentException si el valor no es Base64 válido
     */
    default NumeroTarjeta stringToNumeroTarjeta(String numeroEncriptado) {
        if (numeroEncriptado == null) {
            return null;
        }

        // ✅ Validación: verificar que es Base64 válido
        if (!numeroEncriptado.matches("^[A-Za-z0-9+/=]+$")) {
            throw new IllegalArgumentException(
                    "El número encriptado contiene caracteres inválidos. " +
                    "Se esperaba Base64, se recibió: '" + numeroEncriptado + "'. " +
                    "Esto indica que el mapper guardó incorrectamente los datos en la BD."
            );
        }

        try {
            return NumeroTarjeta.reconstruido(numeroEncriptado);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Error al reconstruir número de tarjeta desde BD. " +
                    "Valor recibido: '" + numeroEncriptado + "'. " +
                    "Error: " + e.getMessage(),
                    e
            );
        }
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

    /**
     * Extrae los últimos 4 dígitos de un número enmascarado.
     *
     * <p><strong>Input esperado:</strong> "**** **** **** 1234"
     * <p><strong>Output:</strong> "1234"
     *
     * <p><strong>IMPORTANTE:</strong> Este método NO debe usarse sobre
     * el número encriptado en Base64.
     *
     * @param numeroEnmascarado número con formato "**** **** **** 1234"
     * @return últimos 4 dígitos o "****" si no se pueden extraer
     */
    default String extraerUltimosDigitos(String numeroEnmascarado) {
        if (numeroEnmascarado == null || numeroEnmascarado.length() < 4) {
            return "****";
        }

        // Extraer solo dígitos
        String soloDigitos = numeroEnmascarado.replaceAll("[^0-9]", "");

        if (soloDigitos.length() < 4) {
            return "****";
        }

        return soloDigitos.substring(soloDigitos.length() - 4);
    }
}