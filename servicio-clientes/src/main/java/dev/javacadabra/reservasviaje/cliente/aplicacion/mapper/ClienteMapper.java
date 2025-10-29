package dev.javacadabra.reservasviaje.cliente.aplicacion.mapper;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.DatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.DatosPersonales;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.ClienteEntidad;
import org.mapstruct.*;

/**
 * Mapper central para conversiones del agregado Cliente.
 *
 * <p>Maneja las transformaciones entre:
 * <ul>
 *   <li>DTOs de entrada → Agregados de dominio</li>
 *   <li>Agregados de dominio → DTOs de salida</li>
 *   <li>Entidades JPA → Agregados de dominio (reconstrucción)</li>
 *   <li>Agregados de dominio → Entidades JPA (persistencia)</li>
 * </ul>
 *
 * <h3>Estrategia de mapeo:</h3>
 * <ul>
 *   <li>Usa métodos {@code default} para mappings complejos con lógica de negocio</li>
 *   <li>Respeta la inmutabilidad de los value objects del dominio</li>
 *   <li>Utiliza el Builder pattern de DatosPersonales para construcción segura</li>
 *   <li>Delega validaciones al dominio (no duplica lógica aquí)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 2.0.0
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ClienteMapper {

    // ========== MAPEO: DTO ENTRADA → DOMINIO ==========

    /**
     * Convierte DTO de creación a value object DatosPersonales.
     *
     * <p>Usa el Builder de DatosPersonales que internamente realiza todas
     * las validaciones necesarias. Si los datos son inválidos, el Builder
     * lanzará IllegalArgumentException.
     *
     * @param dto DTO con datos personales del cliente
     * @return value object DatosPersonales validado
     * @throws IllegalArgumentException si los datos no cumplen las validaciones
     */
    default DatosPersonales aDatosPersonales(CrearClienteDTO dto) {
        if (dto == null) {
            return null;
        }

        return DatosPersonales.builder()
                .dni(dto.dni())
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .telefono(dto.telefono())
                .fechaNacimiento(dto.fechaNacimiento())
                .build();
    }

    /**
     * Convierte DTO de creación a agregado de dominio Cliente.
     *
     * <p>Primero construye el value object DatosPersonales y luego
     * crea el agregado Cliente usando el método de fábrica del dominio.
     *
     * @param dto DTO con datos para crear cliente
     * @return nuevo agregado Cliente con estado inicial (id=null, activo=true)
     * @throws IllegalArgumentException si los datos personales no son válidos
     */
    default Cliente aCliente(CrearClienteDTO dto) {
        if (dto == null) {
            return null;
        }

        DatosPersonales datosPersonales = aDatosPersonales(dto);

        // Crear cliente usando método de fábrica del dominio
        // El id será null (se asignará al persistir) y activo será true por defecto
        return Cliente.crear(datosPersonales);
    }

    // ========== MAPEO: DOMINIO → DTO SALIDA ==========

    /**
     * Convierte agregado Cliente a DTO de salida.
     *
     * <p>Extrae información del agregado y de sus value objects
     * para construir un DTO plano optimizado para la capa de presentación.
     *
     * @param cliente agregado de dominio
     * @return DTO con información del cliente
     */
    @Mapping(target = "id", source = "id.valor")
    @Mapping(target = "nombreCompleto", expression = "java(cliente.getDatosPersonales().obtenerNombreCompleto())")
    @Mapping(target = "email", source = "datosPersonales.email")
    @Mapping(target = "telefono", source = "datosPersonales.telefono")
    @Mapping(target = "edad", expression = "java(cliente.getDatosPersonales().calcularEdad())")
    @Mapping(target = "activo", source = "activo")
    ClienteDTO aClienteDTO(Cliente cliente);

    /**
     * Convierte DatosPersonales del dominio a DTO de salida.
     *
     * <p>Incluye el DNI enmascarado para cumplir con RGPD y
     * campos calculados como edad y nombre completo.
     *
     * @param datosPersonales value object del dominio
     * @return DTO con datos personales para la capa de presentación
     */
    default DatosPersonalesDTO aDatosPersonalesDTO(DatosPersonales datosPersonales) {
        if (datosPersonales == null) {
            return null;
        }

        return DatosPersonalesDTO.builder()
                .dniEnmascarado(datosPersonales.obtenerDniEnmascarado())
                .nombre(datosPersonales.getNombre())
                .apellidos(datosPersonales.getApellidos())
                .nombreCompleto(datosPersonales.obtenerNombreCompleto())
                .email(datosPersonales.getEmail())
                .telefono(datosPersonales.getTelefono())
                .fechaNacimiento(datosPersonales.getFechaNacimiento())
                .edad(datosPersonales.calcularEdad())
                .build();
    }

    // ========== MAPEO: ACTUALIZACIÓN DATOS PERSONALES ==========

    /**
     * Actualiza los datos personales existentes con los valores del DTO.
     *
     * <p><strong>IMPORTANTE:</strong> Este método NO modifica el objeto original,
     * devuelve una nueva instancia con los datos actualizados (inmutabilidad).
     * El DNI y la fecha de nacimiento se mantienen inmutables.
     *
     * <p>Usa el método de dominio {@code actualizarDatosContacto()} para
     * mantener la lógica de negocio encapsulada en el value object.
     *
     * @param datosActuales datos personales actuales del cliente
     * @param dto DTO con los nuevos datos de contacto
     * @return nueva instancia de DatosPersonales con los datos actualizados
     * @throws IllegalArgumentException si los nuevos datos no son válidos
     */
    default DatosPersonales actualizarDatosPersonales(
            DatosPersonales datosActuales,
            ActualizarDatosPersonalesDTO dto) {

        if (dto == null || datosActuales == null) {
            return datosActuales;
        }

        // Delega al método del value object para mantener la lógica en el dominio
        return datosActuales.actualizarDatosContacto(
                dto.nombre(),
                dto.apellidos(),
                dto.email(),
                dto.telefono()
        );
    }

    // ========== MAPEO: ENTIDAD JPA → DOMINIO ==========

    /**
     * Reconstruye el agregado Cliente desde la entidad JPA.
     *
     * <p>Utiliza métodos estáticos de reconstrucción del dominio
     * para asegurar que el estado reconstruido es válido.
     *
     * <p>Este método NO valida los datos porque se asume que la
     * entidad JPA ya fue validada al persistirse originalmente.
     *
     * @param entidad entidad JPA persistida
     * @return agregado Cliente reconstruido desde la base de datos
     */
    default Cliente aDominio(ClienteEntidad entidad) {
        if (entidad == null) {
            return null;
        }

        // Reconstruir DatosPersonales usando el Builder
        DatosPersonales datosPersonales = DatosPersonales.builder()
                .dni(entidad.getDni())
                .nombre(entidad.getNombre())
                .apellidos(entidad.getApellidos())
                .email(entidad.getEmail())
                .telefono(entidad.getTelefono())
                .fechaNacimiento(entidad.getFechaNacimiento())
                .build();

        // Reconstruir Cliente usando método estático del agregado
        return Cliente.reconstruir(
                ClienteId.de(entidad.getId()),
                datosPersonales,
                entidad.isActivo()
        );
    }

    // ========== MAPEO: DOMINIO → ENTIDAD JPA ==========

    /**
     * Convierte agregado Cliente a entidad JPA para persistencia.
     *
     * <p>Descompone el agregado y sus value objects en campos
     * planos de la entidad JPA para mapeo relacional.
     *
     * @param cliente agregado de dominio
     * @return entidad JPA lista para persistir
     */
    @Mapping(target = "id", source = "id.valor")
    @Mapping(target = "dni", source = "datosPersonales.dni")
    @Mapping(target = "nombre", source = "datosPersonales.nombre")
    @Mapping(target = "apellidos", source = "datosPersonales.apellidos")
    @Mapping(target = "email", source = "datosPersonales.email")
    @Mapping(target = "telefono", source = "datosPersonales.telefono")
    @Mapping(target = "fechaNacimiento", source = "datosPersonales.fechaNacimiento")
    @Mapping(target = "activo", source = "activo")
    ClienteEntidad aEntidad(Cliente cliente);

    /**
     * Actualiza una entidad JPA existente con datos del agregado.
     *
     * <p>Útil para operaciones de actualización donde se quiere
     * mantener la misma instancia de entidad (ej: Hibernate merge).
     *
     * @param cliente agregado de dominio con datos actualizados
     * @param entidad entidad JPA existente a actualizar
     */
    @Mapping(target = "id", ignore = true) // El ID no se puede cambiar
    @Mapping(target = "dni", source = "datosPersonales.dni")
    @Mapping(target = "nombre", source = "datosPersonales.nombre")
    @Mapping(target = "apellidos", source = "datosPersonales.apellidos")
    @Mapping(target = "email", source = "datosPersonales.email")
    @Mapping(target = "telefono", source = "datosPersonales.telefono")
    @Mapping(target = "fechaNacimiento", source = "datosPersonales.fechaNacimiento")
    @Mapping(target = "activo", source = "activo")
    void actualizarEntidad(Cliente cliente, @MappingTarget ClienteEntidad entidad);

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Convierte ID primitivo a value object ClienteId.
     *
     * @param id ID primitivo (puede ser null si el cliente no está persistido)
     * @return value object ClienteId o null
     */
    default ClienteId mapClienteId(String id) {
        return id != null ? ClienteId.de(id) : null;
    }

    /**
     * Convierte ClienteId a ID primitivo.
     *
     * @param clienteId value object ClienteId
     * @return ID primitivo o null
     */
    default String mapClienteId(ClienteId clienteId) {
        return clienteId != null ? clienteId.valor() : null;
    }
}