package dev.javacadabra.reservasviaje.cliente.dominio.repositorio;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (repositorio) para el agregado Cliente.
 *
 * <p>Esta interfaz define el contrato para la persistencia del agregado
 * Cliente, siguiendo el patrón Repository de DDD.
 *
 * <p>En arquitectura hexagonal, esta interfaz es un <strong>puerto</strong>
 * definido en el dominio, y su implementación concreta (adaptador) reside
 * en la capa de infraestructura.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Persistir y recuperar agregados Cliente completos</li>
 *   <li>Garantizar la unicidad de email y DNI</li>
 *   <li>Mantener la integridad transaccional del agregado</li>
 *   <li>No exponer detalles de implementación de persistencia</li>
 * </ul>
 *
 * <p><strong>Nota importante:</strong> Este repositorio trabaja con el agregado
 * completo (Cliente + TarjetaCredito), no con entidades individuales.
 * Las tarjetas se gestionan a través del agregado Cliente.
 *
 * @author javacadabra
 * @version 1.0.0
 */
public interface ClienteRepositorio {

    /**
     * Guarda o actualiza un cliente en el repositorio.
     *
     * <p>Si el cliente es nuevo (no existe en BD), se persiste por primera vez.
     * Si el cliente ya existe, se actualiza con los nuevos valores.
     *
     * <p>Esta operación es transaccional: se persiste el agregado completo
     * (cliente + tarjetas) en una sola transacción.
     *
     * @param cliente agregado cliente a guardar
     * @return cliente guardado con datos actualizados (ej: timestamps)
     * @throws IllegalArgumentException si el cliente es nulo
     */
    Cliente save(Cliente cliente);

    /**
     * Busca un cliente por su identificador único.
     *
     * @param clienteId identificador del cliente
     * @return Optional con el cliente si existe, Optional.empty() en caso contrario
     * @throws IllegalArgumentException si el clienteId es nulo
     */
    Optional<Cliente> findById(ClienteId clienteId);

    /**
     * Busca un cliente por su email.
     *
     * <p>El email es único en el sistema, por lo que solo puede
     * haber un cliente con un email determinado.
     *
     * @param email email del cliente
     * @return Optional con el cliente si existe, Optional.empty() en caso contrario
     * @throws IllegalArgumentException si el email es nulo o vacío
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca un cliente por su DNI.
     *
     * <p>El DNI es único en el sistema, por lo que solo puede
     * haber un cliente con un DNI determinado.
     *
     * @param dni DNI del cliente (formato normalizado: 12345678Z)
     * @return Optional con el cliente si existe, Optional.empty() en caso contrario
     * @throws IllegalArgumentException si el dni es nulo o vacío
     */
    Optional<Cliente> findByDni(String dni);

    /**
     * Verifica si existe un cliente con el email especificado.
     *
     * <p>Útil para validar unicidad de email antes de crear un nuevo cliente.
     *
     * @param email email a verificar
     * @return true si existe un cliente con ese email, false en caso contrario
     * @throws IllegalArgumentException si el email es nulo o vacío
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un cliente con el DNI especificado.
     *
     * <p>Útil para validar unicidad de DNI antes de crear un nuevo cliente.
     *
     * @param dni DNI a verificar (formato normalizado: 12345678Z)
     * @return true si existe un cliente con ese DNI, false en caso contrario
     * @throws IllegalArgumentException si el dni es nulo o vacío
     */
    boolean existsByDni(String dni);

    /**
     * Elimina un cliente del repositorio.
     *
     * <p><strong>ADVERTENCIA:</strong> Esta operación elimina el cliente
     * y todas sus tarjetas asociadas de forma permanente.
     *
     * <p>En la mayoría de casos, es preferible usar el método
     * {@link Cliente#desactivar()} en lugar de eliminar físicamente.
     *
     * @param cliente cliente a eliminar
     * @throws IllegalArgumentException si el cliente es nulo
     */
    void delete(Cliente cliente);

    /**
     * Elimina un cliente por su identificador.
     *
     * <p><strong>ADVERTENCIA:</strong> Esta operación elimina el cliente
     * y todas sus tarjetas asociadas de forma permanente.
     *
     * @param clienteId identificador del cliente a eliminar
     * @throws IllegalArgumentException si el clienteId es nulo
     */
    void deleteById(ClienteId clienteId);

    /**
     * Obtiene todos los clientes del sistema.
     *
     * <p><strong>ADVERTENCIA:</strong> Este método puede retornar una cantidad
     * grande de datos. Considerar usar paginación en capa de aplicación
     * para consultas de listados.
     *
     * @return lista de todos los clientes (puede estar vacía)
     */
    List<Cliente> findAll();

    /**
     * Cuenta la cantidad total de clientes en el sistema.
     *
     * @return cantidad de clientes
     */
    long count();

    /**
     * Verifica si existe un cliente con el ID especificado.
     *
     * @param clienteId identificador del cliente
     * @return true si existe, false en caso contrario
     * @throws IllegalArgumentException si el clienteId es nulo
     */
    boolean existsById(ClienteId clienteId);
}