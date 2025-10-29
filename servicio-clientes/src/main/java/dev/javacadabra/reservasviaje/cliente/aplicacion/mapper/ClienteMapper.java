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
 * manteniendo la inmutabilidad del dominio y delegando la lógica
 * de negocio al dominio mismo.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Mapeo de agregados a DTOs de salida</li>
 *   <li>Construcción de value objects desde DTOs de entrada</li>
 *   <li>Cálculo de campos derivados para DTOs</li>
 *   <li>Conversión de colecciones</li>
 * </ul>
 *
 * @author javacadabra
 * @version 2.0.0
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TarjetaCreditoMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClienteMapper {

    // ==================== DOMINIO → DTO COMPLETO ====================

    /**
     * Mapea el agregado Cliente completo a DTO de salida.
     *
     * @param cliente agregado de dominio
     * @return DTO completo con toda la información del cliente
     */
    @Mapping(target = "clienteId", source = "clienteId", qualifiedByName = "clienteIdToString")
    @Mapping(target = "datosPersonales", source = "datosPersonales", qualifiedByName = "toDatosPersonalesDTO")
    @Mapping(target = "direccion", source = "direccion", qualifiedByName = "toDireccionDTO")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToString")
    @Mapping(target = "estadoDescripcion", source = "estado.descripcion")
    @Mapping(target = "tarjetas", source = "tarjetas")
    @Mapping(target = "cantidadTarjetas", expression = "java(cliente.getCantidadTarjetas())")
    @Mapping(target = "tieneTarjetasValidas", expression = "java(cliente.tieneTarjetasValidas())")
    @Mapping(target = "puedeRealizarPagos", expression = "java(cliente.puedeRealizarReservas())")
    @Mapping(target = "estaActivo", expression = "java(cliente.estaActivo())")
    @Mapping(target = "estaBloqueado", expression = "java(cliente.estaBloqueado())")
    @Mapping(target = "estaEnProcesoReserva", expression = "java(cliente.estaEnProcesoReserva())")
    @Mapping(target = "motivoBloqueo", source = "motivoBloqueo")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    @Mapping(target = "fechaActualizacion", source = "fechaActualizacion")
    ClienteDTO aDTO(Cliente cliente);

    /**
     * Mapea una lista de clientes a DTOs completos.
     *
     * @param clientes lista de agregados
     * @return lista de DTOs completos
     */
    List<ClienteDTO> aDTOList(List<Cliente> clientes);

    // ==================== DOMINIO → DTO RESUMIDO ====================

    /**
     * Mapea el agregado Cliente a DTO resumido (para listados).
     *
     * @param cliente agregado de dominio
     * @return DTO resumido con información básica
     */
    @Mapping(target = "clienteId", source = "clienteId", qualifiedByName = "clienteIdToString")
    @Mapping(target = "nombreCompleto", expression = "java(cliente.getDatosPersonales().obtenerNombreCompleto())")
    @Mapping(target = "email", source = "datosPersonales.email")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToString")
    @Mapping(target = "estadoDescripcion", source = "estado.descripcion")
    @Mapping(target = "ciudadResidencia", source = "direccion.ciudad")
    @Mapping(target = "paisResidencia", source = "direccion.pais")
    @Mapping(target = "cantidadTarjetas", expression = "java(cliente.getCantidadTarjetas())")
    @Mapping(target = "puedeRealizarPagos", expression = "java(cliente.puedeRealizarReservas())")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    ClienteResumenDTO aResumenDTO(Cliente cliente);

    /**
     * Mapea lista de clientes a DTOs resumidos.
     *
     * @param clientes lista de agregados
     * @return lista de DTOs resumidos
     */
    List<ClienteResumenDTO> toResumenDTOList(List<Cliente> clientes);

    // ==================== VALUE OBJECTS → DTOs ====================

    /**
     * Mapea DatosPersonales a DTO de salida.
     * NOTA: DatosPersonales no tiene DNI, por eso no se mapea.
     *
     * @param datosPersonales value object de dominio
     * @return DTO con datos personales (sin DNI)
     */
    @Named("toDatosPersonalesDTO")
    default DatosPersonalesDTO toDatosPersonalesDTO(DatosPersonales datosPersonales) {
        if (datosPersonales == null) {
            return null;
        }

        return new DatosPersonalesDTO(
                null, // dniEnmascarado - DatosPersonales no tiene DNI
                datosPersonales.nombre(),
                datosPersonales.apellidos(),
                datosPersonales.obtenerNombreCompleto(),
                datosPersonales.email(),
                datosPersonales.telefono(),
                datosPersonales.fechaNacimiento(),
                datosPersonales.calcularEdad()
        );
    }

    /**
     * Mapea Direccion a DTO de salida.
     *
     * @param direccion value object de dominio
     * @return DTO con dirección completa
     */
    @Named("toDireccionDTO")
    default DireccionDTO toDireccionDTO(Direccion direccion) {
        if (direccion == null) {
            return null;
        }

        return new DireccionDTO(
                direccion.calle(),
                direccion.ciudad(),
                direccion.codigoPostal(),
                direccion.provincia(),
                direccion.pais(),
                direccion.obtenerDireccionCompleta(),
                direccion.obtenerDireccionResumida()
        );
    }

    // ==================== DTOs ENTRADA → VALUE OBJECTS ====================

    /**
     * Construye DatosPersonales desde CrearClienteDTO.
     *
     * @param dto DTO de entrada para crear cliente
     * @return value object DatosPersonales validado
     */
    default DatosPersonales toDatosPersonales(CrearClienteDTO dto) {
        if (dto == null) {
            return null;
        }

        return new DatosPersonales(
                dto.nombre(),
                dto.apellidos(),
                dto.email(),
                dto.telefono(),
                dto.fechaNacimiento()
        );
    }

    /**
     * Construye DatosPersonales desde ActualizarDatosPersonalesDTO.
     * Mantiene el DNI original (inmutable) y la fecha de nacimiento original.
     *
     * @param dto DTO de entrada para actualizar datos personales
     * @param datosActuales datos personales actuales (para mantener DNI y fecha nacimiento)
     * @return value object DatosPersonales actualizado
     */
    default DatosPersonales toDatosPersonales(ActualizarDatosPersonalesDTO dto, DatosPersonales datosActuales) {
        if (dto == null || datosActuales == null) {
            return datosActuales;
        }

        return new DatosPersonales(
                dto.nombre(),
                dto.apellidos(),
                dto.email(),
                dto.telefono(),
                datosActuales.fechaNacimiento() // Mantener fecha nacimiento original (inmutable)
        );
    }

    /**
     * Construye Direccion desde CrearClienteDTO.
     *
     * @param dto DTO de entrada para crear cliente
     * @return value object Direccion validado
     */
    default Direccion toDireccion(CrearClienteDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Direccion(
                dto.calle(),
                dto.ciudad(),
                dto.obtenerCodigoPostalNormalizado(),
                dto.provincia(),
                dto.pais()
        );
    }

    /**
     * Construye Direccion desde ActualizarDireccionDTO.
     *
     * @param dto DTO de entrada para actualizar dirección
     * @return value object Direccion validado
     */
    default Direccion aDireccion(ActualizarDireccionDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Direccion(
                dto.calle(),
                dto.ciudad(),
                dto.obtenerCodigoPostalNormalizado(),
                dto.provincia(),
                dto.pais()
        );
    }

    // ==================== CONVERSIONES DE TIPOS ====================

    /**
     * Convierte ClienteId a String.
     *
     * @param clienteId value object ClienteId
     * @return UUID como String
     */
    @Named("clienteIdToString")
    default String clienteIdToString(ClienteId clienteId) {
        return clienteId != null ? clienteId.valor() : null;
    }

    /**
     * Convierte EstadoCliente a String.
     *
     * @param estado enum EstadoCliente
     * @return nombre del estado como String
     */
    @Named("estadoToString")
    default String estadoToString(EstadoCliente estado) {
        return estado != null ? estado.name() : null;
    }

    /**
     * Convierte String a ClienteId.
     *
     * @param clienteId UUID como String
     * @return value object ClienteId
     */
    default ClienteId toClienteId(String clienteId) {
        return clienteId != null ? ClienteId.de(clienteId) : null;
    }
}