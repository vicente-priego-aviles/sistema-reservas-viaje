package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.adaptador;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import dev.javacadabra.reservasviaje.cliente.dominio.repositorio.ClienteRepositorio;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.ClienteEntidad;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.TarjetaCreditoEntidad;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.mapper.ClienteEntidadMapper;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.mapper.TarjetaCreditoEntidadMapper;
import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.repositorio.ClienteRepositorioSpringData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto ClienteRepositorio usando Spring Data JPA.
 *
 * <p>Este adaptador de salida conecta el dominio con la capa de persistencia,
 * convirtiendo entre agregados de dominio y entidades JPA.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Implementar el puerto de salida definido en el dominio</li>
 *   <li>Convertir agregados de dominio a entidades JPA y viceversa</li>
 *   <li>Delegar operaciones CRUD a Spring Data JPA</li>
 *   <li>Gestionar la bidireccionalidad de relaciones JPA</li>
 *   <li>Mantener la consistencia del agregado en persistencia</li>
 * </ul>
 *
 * <p><strong>Patrón Arquitectónico:</strong>
 * Este componente es un <strong>Adaptador de Salida</strong> en arquitectura hexagonal,
 * implementando un puerto de salida definido en el dominio.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ClienteRepositorioAdaptador implements ClienteRepositorio {

    private final ClienteRepositorioSpringData repositorioSpringData;
    private final ClienteEntidadMapper clienteMapper;
    private final TarjetaCreditoEntidadMapper tarjetaMapper;

    // ==================== OPERACIONES DE ESCRITURA ====================

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        log.debug("💾 Guardando cliente: {}", cliente.getClienteId());

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        // Buscar si ya existe la entidad JPA
        Optional<ClienteEntidad> entidadExistente =
                repositorioSpringData.findByIdWithTarjetas(cliente.getClienteId().valor());

        ClienteEntidad entidadJPA;

        if (entidadExistente.isPresent()) {
            // Actualizar entidad existente
            log.debug("🔄 Actualizando cliente existente: {}", cliente.getClienteId());
            entidadJPA = entidadExistente.get();
            actualizarEntidadExistente(cliente, entidadJPA);
        } else {
            // Crear nueva entidad
            log.debug("✨ Creando nuevo cliente: {}", cliente.getClienteId());
            entidadJPA = clienteMapper.aEntidad(cliente);

            // Establecer bidireccionalidad para tarjetas
            if (entidadJPA.getTarjetas() != null) {
                entidadJPA.getTarjetas().forEach(tarjeta -> tarjeta.setCliente(entidadJPA));
            }
        }

        // Persistir
        ClienteEntidad entidadGuardada = repositorioSpringData.save(entidadJPA);

        log.debug("✅ Cliente guardado exitosamente: {}", entidadGuardada.getId());

        // Convertir de vuelta a dominio
        return clienteMapper.aDominio(entidadGuardada);
    }

    @Override
    @Transactional
    public void delete(Cliente cliente) {
        log.debug("🗑️ Eliminando cliente: {}", cliente.getClienteId());

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        repositorioSpringData.deleteById(cliente.getClienteId().valor());

        log.debug("✅ Cliente eliminado exitosamente: {}", cliente.getClienteId());
    }

    @Override
    @Transactional
    public void deleteById(ClienteId clienteId) {
        log.debug("🗑️ Eliminando cliente por ID: {}", clienteId);

        if (clienteId == null) {
            throw new IllegalArgumentException("El clienteId no puede ser nulo");
        }

        repositorioSpringData.deleteById(clienteId.valor());

        log.debug("✅ Cliente eliminado exitosamente: {}", clienteId);
    }

    // ==================== OPERACIONES DE LECTURA ====================

    @Override
    public Optional<Cliente> findById(ClienteId clienteId) {
        log.debug("🔍 Buscando cliente por ID: {}", clienteId);

        if (clienteId == null) {
            throw new IllegalArgumentException("El clienteId no puede ser nulo");
        }

        return repositorioSpringData.findByIdWithTarjetas(clienteId.valor())
                .map(entidad -> {
                    log.debug("✅ Cliente encontrado: {}", clienteId);
                    return clienteMapper.aDominio(entidad);
                });
    }

    @Override
    public Optional<Cliente> findByEmail(String email) {
        log.debug("🔍 Buscando cliente por email: {}", email);

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede ser nulo o vacío");
        }

        return repositorioSpringData.findByEmailIgnoreCase(email)
                .map(entidad -> {
                    log.debug("✅ Cliente encontrado por email: {}", email);
                    return clienteMapper.aDominio(entidad);
                });
    }

    @Override
    public Optional<Cliente> findByDni(String dni) {
        log.debug("🔍 Buscando cliente por DNI");

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI no puede ser nulo o vacío");
        }

        return repositorioSpringData.findByDni(dni)
                .map(entidad -> {
                    log.debug("✅ Cliente encontrado por DNI");
                    return clienteMapper.aDominio(entidad);
                });
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("🔍 Verificando existencia de email: {}", email);

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede ser nulo o vacío");
        }

        return repositorioSpringData.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByDni(String dni) {
        log.debug("🔍 Verificando existencia de DNI");

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI no puede ser nulo o vacío");
        }

        return repositorioSpringData.existsByDni(dni);
    }

    @Override
    public List<Cliente> findAll() {
        log.debug("🔍 Obteniendo todos los clientes");

        List<ClienteEntidad> entidades = repositorioSpringData.findAll();

        log.debug("✅ Se encontraron {} clientes", entidades.size());

        return clienteMapper.aDominioList(entidades);
    }

    @Override
    public long count() {
        return repositorioSpringData.count();
    }

    @Override
    public boolean existsById(ClienteId clienteId) {
        log.debug("🔍 Verificando existencia de cliente: {}", clienteId);

        if (clienteId == null) {
            throw new IllegalArgumentException("El clienteId no puede ser nulo");
        }

        return repositorioSpringData.existsById(clienteId.valor());
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Actualiza una entidad JPA existente con datos del agregado de dominio.
     *
     * <p>Sincroniza la colección de tarjetas manteniendo la bidireccionalidad
     * y respetando las reglas de JPA para orphan removal.
     *
     * @param cliente agregado de dominio con datos actualizados
     * @param entidadExistente entidad JPA a actualizar
     */
    private void actualizarEntidadExistente(Cliente cliente, ClienteEntidad entidadExistente) {
        log.debug("🔄 Actualizando datos de entidad existente");

        // Actualizar campos simples
        clienteMapper.updateEntidadJPA(cliente, entidadExistente);

        // Sincronizar colección de tarjetas
        sincronizarTarjetas(cliente, entidadExistente);
    }

    /**
     * Sincroniza la colección de tarjetas entre dominio y JPA.
     *
     * <p>Este método maneja correctamente la bidireccionalidad y el orphan removal:
     * <ul>
     *   <li>Elimina tarjetas que ya no están en el dominio</li>
     *   <li>Agrega nuevas tarjetas del dominio</li>
     *   <li>Actualiza tarjetas existentes</li>
     *   <li>Mantiene la relación bidireccional cliente-tarjeta</li>
     * </ul>
     *
     * @param cliente agregado de dominio
     * @param entidadJPA entidad JPA
     */
    private void sincronizarTarjetas(Cliente cliente, ClienteEntidad entidadJPA) {
        log.debug("🔄 Sincronizando tarjetas del cliente");

        // Limpiar tarjetas existentes (orphan removal las eliminará de BD)
        entidadJPA.getTarjetas().clear();

        // Agregar tarjetas del dominio
        List<TarjetaCreditoEntidad> nuevasTarjetasJPA =
                tarjetaMapper.aEntidadList(cliente.obtenerTarjetas());

        // Establecer bidireccionalidad y agregar a la colección
        nuevasTarjetasJPA.forEach(tarjeta -> {
            tarjeta.setCliente(entidadJPA);
            entidadJPA.getTarjetas().add(tarjeta);
        });

        log.debug("✅ Tarjetas sincronizadas: {} tarjetas", entidadJPA.getTarjetas().size());
    }
}