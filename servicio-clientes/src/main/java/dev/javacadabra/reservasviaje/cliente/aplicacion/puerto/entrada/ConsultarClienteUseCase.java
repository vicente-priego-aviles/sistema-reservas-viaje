package dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteResumenDTO;

import java.util.List;

/**
 * Puerto de entrada (use case) para consultar información de clientes.
 *
 * <p>Define el contrato para los casos de uso de consulta y búsqueda
 * de clientes en el sistema.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Buscar clientes por diferentes criterios</li>
 *   <li>Mapear agregados de dominio a DTOs de salida</li>
 *   <li>Retornar información completa o resumida según necesidad</li>
 *   <li>Manejar casos de cliente no encontrado</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public interface ConsultarClienteUseCase {

    /**
     * Busca un cliente por su identificador único.
     *
     * <p>Retorna toda la información del cliente incluyendo datos personales,
     * dirección y tarjetas (con números enmascarados).
     *
     * @param clienteId identificador del cliente
     * @return cliente con toda su información
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si el cliente no existe
     */
    ClienteDTO buscarPorId(String clienteId);

    /**
     * Busca un cliente por su email.
     *
     * <p>El email es único en el sistema, por lo que solo puede
     * retornar un cliente o lanzar excepción si no existe.
     *
     * @param email email del cliente
     * @return cliente con toda su información
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si no existe cliente con ese email
     */
    ClienteDTO buscarPorEmail(String email);

    /**
     * Busca un cliente por su DNI.
     *
     * <p>El DNI es único en el sistema, por lo que solo puede
     * retornar un cliente o lanzar excepción si no existe.
     *
     * @param dni DNI del cliente (formato normalizado: 12345678Z)
     * @return cliente con toda su información
     * @throws dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion
     *         si no existe cliente con ese DNI
     */
    ClienteDTO buscarPorDni(String dni);

    /**
     * Lista todos los clientes del sistema (versión resumida).
     *
     * <p><strong>ADVERTENCIA:</strong> Este método puede retornar una gran
     * cantidad de datos. Se recomienda usar paginación en producción.
     *
     * @return lista de todos los clientes en versión resumida
     */
    List<ClienteResumenDTO> listarTodos();

    /**
     * Verifica si existe un cliente con el email especificado.
     *
     * <p>Útil para validaciones antes de crear o actualizar clientes.
     *
     * @param email email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existePorEmail(String email);

    /**
     * Verifica si existe un cliente con el DNI especificado.
     *
     * <p>Útil para validaciones antes de crear clientes.
     *
     * @param dni DNI a verificar (formato normalizado: 12345678Z)
     * @return true si existe, false en caso contrario
     */
    boolean existePorDni(String dni);

    /**
     * Cuenta la cantidad total de clientes en el sistema.
     *
     * @return cantidad de clientes
     */
    long contarClientes();
}