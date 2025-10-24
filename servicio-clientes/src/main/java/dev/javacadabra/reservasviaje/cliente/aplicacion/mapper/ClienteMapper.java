package dev.javacadabra.reservasviaje.cliente.aplicacion.mapper;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDireccionDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteResumenDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.DatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.DireccionDTO;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper para convertir entre Cliente (agregado de dominio) y DTOs.
 *
 * <p>Sigue las mejores prácticas DDD para mappers con MapStruct,
 * manteniendo la inmutabilidad del dominio.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TarjetaCreditoMapper.class}
)
public interface ClienteMapper {

    // ==================== DOMINIO → DTO ====================

    /**
     * Convierte Cliente (dominio) a ClienteDTO completo.
     *
     * @param cliente agregado de dominio
     * @return DTO con todos los datos del cliente
     */
    @Mapping(target = "clienteId", source = "clienteId", qualifiedByName = "clienteIdToString")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToString")
    @Mapping(target = "puedeRealizarReservas", expression = "java(cliente.puedeRealizarReservas())")
    @Mapping(target = "tieneTarjetasValidas", expression = "java(cliente.tieneTarjetasValidas())")
    @Mapping(target = "cantidadTarjetas", expression = "java(cliente.getCantidadTarjetas())")
    @Mapping(target = "estaBloqueado", expression = "java(cliente.estaBloqueado())")
    ClienteDTO aDTO(Cliente cliente);

    /**
     * Convierte lista de Clientes a lista de DTOs completos.
     *
     * @param clientes lista de agregados de dominio
     * @return lista de DTOs
     */
    List<ClienteDTO> aDTOList(List<Cliente> clientes);

    /**
     * Convierte Cliente a ClienteResumenDTO (versión simplificada).
     *
     * @param cliente agregado de dominio
     * @return DTO resumido sin tarjetas ni detalles completos
     */
    @Mapping(target = "clienteId", source = "clienteId", qualifiedByName = "clienteIdToString")
    @Mapping(target = "nombreCompleto", expression = "java(cliente.getDatosPersonales().obtenerNombreCompleto())")
    @Mapping(target = "email", source = "datosPersonales.email")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToString")
    @Mapping(target = "cantidadTarjetas", expression = "java(cliente.getCantidadTarjetas())")
    ClienteResumenDTO aResumenDTO(Cliente cliente);

    /**
     * Convierte lista de Clientes a lista de DTOs resumidos.
     *
     * @param clientes lista de agregados de dominio
     * @return lista de DTOs resumidos
     */
    List<ClienteResumenDTO> toResumenDTOList(List<Cliente> clientes);

    /**
     * Convierte DatosPersonales a DTO.
     *
     * @param datosPersonales value object de dominio
     * @return DTO con datos personales
     */
    DatosPersonalesDTO aDatosPersonalesDTO(DatosPersonales datosPersonales);

    /**
     * Convierte Direccion a DTO.
     *
     * @param direccion value object de dominio
     * @return DTO con dirección
     */
    DireccionDTO aDireccionDTO(Direccion direccion);

    // ==================== DTO → VALUE OBJECTS ====================

    /**
     * Convierte CrearClienteDTO a DatosPersonales (Value Object).
     *
     * @param dto DTO de entrada para crear cliente
     * @return Value Object DatosPersonales
     */
    default DatosPersonales toDatosPersonales(CrearClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DatosPersonales(
                dto.dni(),
                dto.nombre(),
                dto.apellidos(),
                dto.email(),
                dto.telefono(),
                dto.fechaNacimiento()
        );
    }

    /**
     * Convierte ActualizarDatosPersonalesDTO a DatosPersonales preservando DNI.
     *
     * @param dto DTO de entrada para actualizar
     * @param dniActual DNI actual que no se puede modificar
     * @return Value Object DatosPersonales actualizado
     */
    default DatosPersonales toDatosPersonales(ActualizarDatosPersonalesDTO dto, String dniActual) {
        if (dto == null) {
            return null;
        }
        return new DatosPersonales(
                dniActual, // ✅ Preservar DNI original
                dto.nombre(),
                dto.apellidos(),
                dto.email(),
                dto.telefono(),
                null  // fechaNacimiento no se actualiza
        );
    }

    /**
     * Convierte CrearClienteDTO a Direccion (Value Object).
     *
     * @param dto DTO de entrada para crear cliente
     * @return Value Object Direccion
     */
    default Direccion toDireccion(CrearClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Direccion(
                dto.calle(),
                dto.ciudad(),
                dto.codigoPostal(),
                dto.provincia(),
                dto.pais()
        );
    }

    /**
     * Convierte ActualizarDireccionDTO a Direccion (Value Object).
     *
     * @param dto DTO de entrada para actualizar dirección
     * @return Value Object Direccion
     */
    default Direccion aDireccion(ActualizarDireccionDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Direccion(
                dto.calle(),
                dto.ciudad(),
                dto.codigoPostal(),
                dto.provincia(),
                dto.pais()
        );
    }

    // ==================== CONVERSIONES ====================

    /**
     * Convierte ClienteId a String.
     *
     * @param clienteId identificador del cliente
     * @return valor del ID como String
     */
    @Named("clienteIdToString")
    default String clienteIdToString(ClienteId clienteId) {
        return clienteId != null ? clienteId.valor() : null;
    }

    /**
     * Convierte EstadoCliente enum a String.
     *
     * @param estado estado del cliente
     * @return nombre del enum
     */
    @Named("estadoToString")
    default String estadoToString(EstadoCliente estado) {
        return estado != null ? estado.name() : null;
    }
}