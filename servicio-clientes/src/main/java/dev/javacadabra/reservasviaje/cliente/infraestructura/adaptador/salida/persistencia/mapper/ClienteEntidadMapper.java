package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.mapper;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.ClienteEntidad;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper para convertir entre Cliente (dominio) y ClienteEntidad (JPA).
 *
 * <p>Sigue las mejores prácticas DDD: usa métodos default para reconstruir
 * el agregado inmutable desde la entidad JPA.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TarjetaCreditoEntidadMapper.class}
)
public interface ClienteEntidadMapper {

    // ==================== DOMINIO → JPA ====================

    @Mapping(target = "id", source = "clienteId", qualifiedByName = "clienteIdToString")
    @Mapping(target = "dni", source = "datosPersonales.dni")
    @Mapping(target = "nombre", source = "datosPersonales.nombre")
    @Mapping(target = "apellidos", source = "datosPersonales.apellidos")
    @Mapping(target = "email", source = "datosPersonales.email")
    @Mapping(target = "telefono", source = "datosPersonales.telefono")
    @Mapping(target = "fechaNacimiento", source = "datosPersonales.fechaNacimiento")
    @Mapping(target = "calle", source = "direccion.calle")
    @Mapping(target = "ciudad", source = "direccion.ciudad")
    @Mapping(target = "codigoPostal", source = "direccion.codigoPostal")
    @Mapping(target = "provincia", source = "direccion.provincia")
    @Mapping(target = "pais", source = "direccion.pais")
    @Mapping(target = "estado", source = "estado", qualifiedByName = "estadoToEnum")
    @Mapping(target = "tarjetas", source = "tarjetas")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    @Mapping(target = "fechaModificacion", source = "fechaActualizacion")
    ClienteEntidad aEntidad(Cliente cliente);

    List<ClienteEntidad> aEntidadList(List<Cliente> clientes);

    // ==================== JPA → DOMINIO ====================

    /**
     * Reconstruye Cliente desde entidad JPA.
     * Usa método default para evitar que MapStruct intente usar setters.
     */
    default Cliente aDominio(ClienteEntidad entidad) {
        if (entidad == null) {
            return null;
        }

        ClienteId clienteId = stringToClienteId(entidad.getId());

        DatosPersonales datosPersonales = new DatosPersonales(
                entidad.getDni(),
                entidad.getNombre(),
                entidad.getApellidos(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.getFechaNacimiento()
        );

        Direccion direccion = new Direccion(
                entidad.getCalle(),
                entidad.getCiudad(),
                entidad.getCodigoPostal(),
                entidad.getProvincia(),
                entidad.getPais()
        );

        EstadoCliente estado = enumToEstado(entidad.getEstado());

        // Delegar mapeo de tarjetas al mapper especializado
        TarjetaCreditoEntidadMapper tarjetaMapper = getTarjetaMapper();
        var tarjetas = tarjetaMapper.aDominioList(entidad.getTarjetas());

        return Cliente.reconstruir(
                clienteId,
                datosPersonales,
                direccion,
                estado,
                tarjetas,
                entidad.getFechaCreacion(),
                entidad.getFechaModificacion(),
                entidad.getMotivoBloqueo()
        );
    }

    default List<Cliente> aDominioList(List<ClienteEntidad> entidades) {
        if (entidades == null) {
            return null;
        }
        return entidades.stream()
                .map(this::aDominio)
                .toList();
    }

    // ==================== CONVERSIONES ====================

    @Named("clienteIdToString")
    default String clienteIdToString(ClienteId clienteId) {
        return clienteId != null ? clienteId.valor() : null;
    }

    default ClienteId stringToClienteId(String id) {
        return id != null ? ClienteId.de(id) : null;
    }

    @Named("estadoToEnum")
    default ClienteEntidad.EstadoClienteEnum estadoToEnum(EstadoCliente estado) {
        if (estado == null) return null;
        return ClienteEntidad.EstadoClienteEnum.valueOf(estado.name());
    }

    default EstadoCliente enumToEstado(ClienteEntidad.EstadoClienteEnum estadoEnum) {
        if (estadoEnum == null) return null;
        return EstadoCliente.valueOf(estadoEnum.name());
    }

    // ==================== MÉTODO AUXILIAR ====================

    /**
     * Obtiene el mapper de tarjetas inyectado por MapStruct.
     */
    @Named("tarjetaMapper")
    TarjetaCreditoEntidadMapper getTarjetaMapper();
}