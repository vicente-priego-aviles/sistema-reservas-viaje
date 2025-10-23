package dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDireccionDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;

/**
 * Puerto de entrada (use case) para actualizar datos de un cliente.
 *
 * <p>Define el contrato para los casos de uso de actualización de
 * información del cliente, incluyendo datos personales y dirección.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar que el cliente existe y puede ser actualizado</li>
 *   <li>Verificar estado del cliente (no bloqueado, no inactivo)</li>
 *   <li>Validar unicidad de email si cambió</li>
 *   <li>Actualizar información del agregado Cliente</li>
 *   <li>Publicar eventos de dominio correspondientes</li>
 *   <li>Persistir cambios</li>
 * </ul>
 *
 * <p><strong>Validaciones de negocio:</strong>
 * <ul>
 *   <li>Cliente debe existir</li>
 *   <li>Cliente no puede estar BLOQUEADO o INACTIVO</li>
 *   <li>Nuevo email debe ser único (si cambió)</li>
 *   <li>Cliente debe ser mayor de 18 años</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public interface ActualizarClienteUseCase {

    /**
     * Actualiza los datos personales de un cliente.
     *
     * <p>Permite actualizar nombre, apellidos, email, teléfono y fecha de nacimiento.
     * El DNI NO puede ser actualizado por motivos de seguridad.
     *
     * <p>Si el email cambia, se publica un evento indicando que requiere revalidación.
     *
     * @param clienteId identificador del cliente a actualizar
     * @param dto nuevos datos personales
     * @return cliente actualizado con toda su información
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion
     *         si el cliente está bloqueado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion
     *         si el cliente está inactivo
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.EmailDuplicadoExcepcion
     *         si el nuevo email ya existe (cuando cambió)
     * @throws IllegalArgumentException si los datos son inválidos
     */
    ClienteDTO actualizarDatosPersonales(String clienteId, ActualizarDatosPersonalesDTO dto);

    /**
     * Actualiza la dirección postal de un cliente.
     *
     * <p>Permite actualizar todos los componentes de la dirección: calle,
     * ciudad, código postal, provincia y país.
     *
     * @param clienteId identificador del cliente a actualizar
     * @param dto nueva dirección
     * @return cliente actualizado con toda su información
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion
     *         si el cliente está bloqueado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion
     *         si el cliente está inactivo
     * @throws IllegalArgumentException si los datos son inválidos
     */
    ClienteDTO actualizarDireccion(String clienteId, ActualizarDireccionDTO dto);
}