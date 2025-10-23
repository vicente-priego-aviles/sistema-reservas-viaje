package dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.AgregarTarjetaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.TarjetaCreditoDTO;

import java.util.List;

/**
 * Puerto de entrada (use case) para gestionar tarjetas de crédito de clientes.
 *
 * <p>Define el contrato para los casos de uso de gestión de tarjetas,
 * respetando las reglas de negocio del agregado Cliente.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Agregar nuevas tarjetas (máximo 3 por cliente)</li>
 *   <li>Eliminar tarjetas existentes (mínimo 1 siempre)</li>
 *   <li>Consultar tarjetas de un cliente</li>
 *   <li>Validar estado del cliente antes de operaciones</li>
 *   <li>Publicar eventos de dominio correspondientes</li>
 * </ul>
 *
 * <p><strong>Reglas de negocio:</strong>
 * <ul>
 *   <li>Un cliente debe tener mínimo 1 tarjeta siempre</li>
 *   <li>Un cliente puede tener máximo 3 tarjetas</li>
 *   <li>Solo clientes ACTIVOS o EN_PROCESO_RESERVA pueden gestionar tarjetas</li>
 *   <li>Clientes BLOQUEADOS o INACTIVOS no pueden gestionar tarjetas</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public interface GestionarTarjetasUseCase {

    /**
     * Agrega una nueva tarjeta de crédito a un cliente.
     *
     * <p>Valida que el cliente no tenga ya 3 tarjetas (límite máximo)
     * y que esté en estado que permita agregar tarjetas.
     *
     * @param clienteId identificador del cliente
     * @param dto datos de la nueva tarjeta
     * @return cliente actualizado con la nueva tarjeta
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.LimiteMaximoTarjetasExcepcion
     *         si el cliente ya tiene 3 tarjetas
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion
     *         si el cliente está bloqueado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion
     *         si el cliente está inactivo
     * @throws IllegalArgumentException si los datos de la tarjeta son inválidos
     */
    ClienteDTO agregarTarjeta(String clienteId, AgregarTarjetaDTO dto);

    /**
     * Elimina una tarjeta de crédito de un cliente.
     *
     * <p>Valida que el cliente tenga más de 1 tarjeta antes de eliminar
     * (debe mantener al menos 1 tarjeta siempre).
     *
     * @param clienteId identificador del cliente
     * @param tarjetaId identificador de la tarjeta a eliminar
     * @param motivo motivo de la eliminación (opcional)
     * @return cliente actualizado sin la tarjeta eliminada
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.TarjetaNoEncontradaExcepcion
     *         si la tarjeta no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteRequiereTarjetaExcepcion
     *         si es la única tarjeta del cliente
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion
     *         si el cliente está bloqueado
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion
     *         si el cliente está inactivo
     */
    ClienteDTO eliminarTarjeta(String clienteId, String tarjetaId, String motivo);

    /**
     * Obtiene todas las tarjetas de un cliente.
     *
     * <p>Las tarjetas se retornan con números enmascarados por seguridad.
     *
     * @param clienteId identificador del cliente
     * @return lista de tarjetas del cliente
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     */
    List<TarjetaCreditoDTO> obtenerTarjetasCliente(String clienteId);

    /**
     * Obtiene solo las tarjetas válidas de un cliente.
     *
     * <p>Una tarjeta es válida si no está expirada, fue validada externamente
     * y no tiene motivo de rechazo.
     *
     * @param clienteId identificador del cliente
     * @return lista de tarjetas válidas del cliente
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     */
    List<TarjetaCreditoDTO> obtenerTarjetasValidasCliente(String clienteId);

    /**
     * Obtiene una tarjeta específica de un cliente.
     *
     * @param clienteId identificador del cliente
     * @param tarjetaId identificador de la tarjeta
     * @return tarjeta solicitada
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.TarjetaNoEncontradaExcepcion
     *         si la tarjeta no existe
     */
    TarjetaCreditoDTO obtenerTarjeta(String clienteId, String tarjetaId);
}