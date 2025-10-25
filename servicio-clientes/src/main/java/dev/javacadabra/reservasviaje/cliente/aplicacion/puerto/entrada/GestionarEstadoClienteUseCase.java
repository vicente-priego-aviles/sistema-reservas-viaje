package dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;

/**
 * Puerto de entrada (use case) para gestionar el ciclo de vida del cliente.
 *
 * <p>Define el contrato para los casos de uso que cambian el estado del cliente,
 * implementando las transiciones permitidas según las reglas de negocio.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Activar clientes (PENDIENTE_VALIDACION → ACTIVO)</li>
 *   <li>Bloquear/desbloquear clientes por seguridad</li>
 *   <li>Gestionar proceso de reserva (estados transitorios)</li>
 *   <li>Desactivar clientes (baja voluntaria)</li>
 *   <li>Validar transiciones de estado permitidas</li>
 *   <li>Publicar eventos de dominio correspondientes</li>
 * </ul>
 *
 * <p><strong>Transiciones de estado permitidas:</strong>
 * <ul>
 *   <li>PENDIENTE_VALIDACION → ACTIVO (activar)</li>
 *   <li>ACTIVO → EN_PROCESO_RESERVA (iniciarProcesoReserva)</li>
 *   <li>EN_PROCESO_RESERVA → RESERVA_CONFIRMADA (confirmarReserva)</li>
 *   <li>RESERVA_CONFIRMADA → ACTIVO (finalizarReserva)</li>
 *   <li>ACTIVO → BLOQUEADO (bloquear)</li>
 *   <li>BLOQUEADO → ACTIVO (desbloquear)</li>
 *   <li>ACTIVO → INACTIVO (desactivar)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public interface GestionarEstadoClienteUseCase {

    /**
     * Activa un cliente tras completar el proceso de validación.
     *
     * <p>El cliente pasa de PENDIENTE_VALIDACION a ACTIVO, lo que le
     * permite realizar Pagos de viajes.
     *
     * @param clienteId identificador del cliente
     * @return cliente activado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws IllegalStateException si el cliente no está en PENDIENTE_VALIDACION
     */
    ClienteDTO activarCliente(String clienteId);

    /**
     * Bloquea un cliente por motivos de seguridad.
     *
     * <p>Un cliente bloqueado no puede realizar ninguna operación hasta
     * que sea desbloqueado por un administrador.
     *
     * @param clienteId identificador del cliente
     * @param motivo motivo del bloqueo (requerido)
     * @param requiereRevisionManual si requiere revisión manual de administrador
     * @return cliente bloqueado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion
     *         si el cliente está inactivo
     * @throws IllegalArgumentException si el motivo es nulo o vacío
     */
    ClienteDTO bloquearCliente(String clienteId, String motivo, boolean requiereRevisionManual);

    /**
     * Desbloquea un cliente previamente bloqueado.
     *
     * <p>Requiere especificar el administrador que autoriza el desbloqueo
     * y el motivo de la decisión.
     *
     * @param clienteId identificador del cliente
     * @param motivoDesbloqueo justificación del desbloqueo (requerido)
     * @param administrador administrador que autoriza (requerido)
     * @return cliente desbloqueado (estado ACTIVO)
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws IllegalStateException si el cliente no está BLOQUEADO
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    ClienteDTO desbloquearCliente(String clienteId, String motivoDesbloqueo, String administrador);

    /**
     * Inicia el proceso de reserva para un cliente.
     *
     * <p>El cliente pasa de ACTIVO a EN_PROCESO_RESERVA.
     * Este es un estado transitorio durante el flujo de reserva de viaje.
     *
     * @param clienteId identificador del cliente
     * @return cliente en proceso de reserva
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion
     *         si el cliente está bloqueado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion
     *         si el cliente está inactivo
     * @throws IllegalStateException si el cliente no está ACTIVO
     */
    ClienteDTO iniciarProcesoReserva(String clienteId);

    /**
     * Confirma la reserva tras un pago exitoso.
     *
     * <p>El cliente pasa de EN_PROCESO_RESERVA a RESERVA_CONFIRMADA.
     *
     * @param clienteId identificador del cliente
     * @return cliente con reserva confirmada
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws IllegalStateException si el cliente no está EN_PROCESO_RESERVA
     */
    ClienteDTO confirmarReserva(String clienteId);

    /**
     * Finaliza el proceso de reserva.
     *
     * <p>El cliente pasa de RESERVA_CONFIRMADA a ACTIVO.
     *
     * @param clienteId identificador del cliente
     * @return cliente activo
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws IllegalStateException si el cliente no está en RESERVA_CONFIRMADA
     */
    ClienteDTO finalizarReserva(String clienteId);

    /**
     * Desactiva un cliente (baja voluntaria).
     *
     * <p>El cliente pasa a estado INACTIVO y no puede realizar operaciones
     * hasta que sea reactivado.
     *
     * @param clienteId identificador del cliente
     * @return cliente desactivado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion
     *         si el cliente está bloqueado (debe desbloquearse primero)
     */
    ClienteDTO desactivarCliente(String clienteId);
}