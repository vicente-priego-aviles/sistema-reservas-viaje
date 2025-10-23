package dev.javacadabra.reservasviaje.cliente.aplicacion.mapper;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDireccionDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.DatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.DireccionDTO;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper para convertir entre Cliente (agregado de dominio) y ClienteDTO.
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

    @Mapping(target = "clienteId", source = "clienteId", qualifiedByName = "clienteIdToString")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToString")
    ClienteDTO aDTO(Cliente cliente);

    List<ClienteDTO> aDTOList(List<Cliente> clientes);

    DatosPersonalesDTO aDatosPersonalesDTO(DatosPersonales datosPersonales);

    DireccionDTO aDireccionDTO(Direccion direccion);

    // ==================== DTO → VALUE OBJECTS ====================

    default DatosPersonales aDatosPersonales(ActualizarDatosPersonalesDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DatosPersonales(
                null, // DNI no se actualiza
                dto.nombre(),
                dto.apellidos(),
                dto.email(),
                dto.telefono(),
                null  // fechaNacimiento no se actualiza
        );
    }

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

    @Named("clienteIdToString")
    default String clienteIdToString(ClienteId clienteId) {
        return clienteId != null ? clienteId.valor() : null;
    }

    @Named("estadoToString")
    default String estadoToString(EstadoCliente estado) {
        return estado != null ? estado.name() : null;
    }
}