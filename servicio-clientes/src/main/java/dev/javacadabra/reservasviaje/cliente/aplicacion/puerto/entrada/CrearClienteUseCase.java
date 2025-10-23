package dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;

/**
 * Puerto de entrada (use case) para crear un nuevo cliente.
 *
 * <p>Define el contrato para el caso de uso de creación de clientes,
 * siguiendo el patrón de arquitectura hexagonal donde los puertos
 * definen las operaciones disponibles sin detalles de implementación.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar datos de entrada (email y DNI únicos)</li>
 *   <li>Crear el agregado Cliente con sus invariantes</li>
 *   <li>Persistir el cliente con su tarjeta inicial</li>
 *   <li>Publicar evento de dominio ClienteCreadoEvento</li>
 *   <li>Retornar el cliente creado</li>
 * </ul>
 *
 * <p><strong>Validaciones de negocio:</strong>
 * <ul>
 *   <li>Email único en el sistema</li>
 *   <li>DNI único en el sistema</li>
 *   <li>Cliente mayor de 18 años</li>
 *   <li>Tarjeta inicial válida (número, fecha, CVV)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public interface CrearClienteUseCase {

    /**
     * Crea un nuevo cliente en el sistema.
     *
     * <p>El cliente se crea en estado PENDIENTE_VALIDACION con una tarjeta inicial.
     * Tras la creación, se publica un evento de dominio que puede ser consumido
     * por otros servicios para enviar emails de bienvenida, iniciar procesos
     * de validación, etc.
     *
     * @param dto datos del cliente a crear
     * @return cliente creado con toda su información
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.EmailDuplicadoExcepcion
     *         si ya existe un cliente con ese email
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.DniDuplicadoExcepcion
     *         si ya existe un cliente con ese DNI
     * @throws IllegalArgumentException si los datos son inválidos
     */
    ClienteDTO crear(CrearClienteDTO dto);
}