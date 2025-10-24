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
 * <p><strong>Patrón usado:</strong>
 * <ul>
 *   <li>Dominio → JPA: MapStruct genera el código automáticamente</li>
 *   <li>JPA → Dominio: Método default que llama a Cliente.reconstruir()</li>
 *   <li>Inyección de mappers: Usa @Context para acceder a otros mappers</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TarjetaCreditoEntidadMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ClienteEntidadMapper {

    // ==================== DOMINIO → JPA ====================

    /**
     * Convierte Cliente (dominio) a ClienteEntidad (JPA).
     * MapStruct genera automáticamente la implementación.
     *
     * @param cliente agregado de dominio
     * @return entidad JPA para persistencia
     */
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
    @Mapping(target = "motivoBloqueo", source = "motivoBloqueo")
    ClienteEntidad aEntidad(Cliente cliente);

    /**
     * Convierte lista de Clientes a lista de ClienteEntidad.
     *
     * @param clientes lista de agregados de dominio
     * @return lista de entidades JPA
     */
    List<ClienteEntidad> aEntidadList(List<Cliente> clientes);

    // ==================== JPA → DOMINIO ====================

    /**
     * Reconstruye Cliente desde entidad JPA.
     *
     * <p>Usa método default para evitar que MapStruct intente usar setters.
     * Llama al método estático Cliente.reconstruir() para mantener la inmutabilidad.
     *
     * @param entidad entidad JPA
     * @param tarjetaMapper mapper de tarjetas inyectado por MapStruct
     * @return agregado de dominio reconstruido
     */
    default Cliente aDominio(ClienteEntidad entidad, @Context TarjetaCreditoEntidadMapper tarjetaMapper) {
        if (entidad == null) {
            return null;
        }

        // Mapear ClienteId
        ClienteId clienteId = stringToClienteId(entidad.getId());

        // Mapear DatosPersonales (Value Object)
        DatosPersonales datosPersonales = new DatosPersonales(
                entidad.getDni(),
                entidad.getNombre(),
                entidad.getApellidos(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.getFechaNacimiento()
        );

        // Mapear Direccion (Value Object)
        Direccion direccion = new Direccion(
                entidad.getCalle(),
                entidad.getCiudad(),
                entidad.getCodigoPostal(),
                entidad.getProvincia(),
                entidad.getPais()
        );

        // Mapear EstadoCliente (enum)
        EstadoCliente estado = enumToEstado(entidad.getEstado());

        // Mapear Tarjetas usando el mapper inyectado
        var tarjetas = tarjetaMapper.aDominioList(entidad.getTarjetas());

        // Reconstruir el agregado usando el método estático
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

    /**
     * Convierte lista de ClienteEntidad a lista de Cliente.
     *
     * @param entidades lista de entidades JPA
     * @param tarjetaMapper mapper de tarjetas inyectado por MapStruct
     * @return lista de agregados de dominio
     */
    default List<Cliente> aDominioList(List<ClienteEntidad> entidades, @Context TarjetaCreditoEntidadMapper tarjetaMapper) {
        if (entidades == null) {
            return null;
        }
        return entidades.stream()
                .map(e -> aDominio(e, tarjetaMapper))
                .toList();
    }

    // ==================== CONVERSIONES ====================

    /**
     * Convierte ClienteId a String para persistencia.
     *
     * @param clienteId identificador del cliente
     * @return valor del ID como String
     */
    @Named("clienteIdToString")
    default String clienteIdToString(ClienteId clienteId) {
        return clienteId != null ? clienteId.valor() : null;
    }

    /**
     * Convierte String a ClienteId al reconstruir desde BD.
     *
     * @param id valor del ID como String
     * @return ClienteId reconstruido
     */
    default ClienteId stringToClienteId(String id) {
        return id != null ? ClienteId.de(id) : null;
    }

    /**
     * Convierte EstadoCliente (dominio) a enum JPA.
     *
     * @param estado estado del dominio
     * @return enum de la entidad JPA
     */
    @Named("estadoToEnum")
    default ClienteEntidad.EstadoClienteEnum estadoToEnum(EstadoCliente estado) {
        if (estado == null) {
            return null;
        }
        return ClienteEntidad.EstadoClienteEnum.valueOf(estado.name());
    }

    /**
     * Convierte enum JPA a EstadoCliente (dominio).
     *
     * @param estadoEnum enum de la entidad JPA
     * @return estado del dominio
     */
    default EstadoCliente enumToEstado(ClienteEntidad.EstadoClienteEnum estadoEnum) {
        if (estadoEnum == null) {
            return null;
        }
        return EstadoCliente.valueOf(estadoEnum.name());
    }
}