package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando se intenta acceder a un cliente
 * que no existe en el sistema.
 *
 * <p>Esto puede ocurrir cuando:
 * <ul>
 *   <li>Se busca un cliente por ID y no existe</li>
 *   <li>Se busca un cliente por email y no existe</li>
 *   <li>Se busca un cliente por DNI y no existe</li>
 *   <li>El cliente fue eliminado del sistema</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class ClienteNoEncontradoExcepcion extends ClienteDominioExcepcion {

    /**
     * Constructor con el ID del cliente no encontrado.
     *
     * @param clienteId identificador del cliente
     */
    public ClienteNoEncontradoExcepcion(String clienteId) {
        super(String.format("El cliente con ID %s no fue encontrado", clienteId));
    }

    /**
     * Constructor con criterio de búsqueda personalizado.
     *
     * @param criterio criterio de búsqueda (ej: "email", "DNI")
     * @param valor valor del criterio de búsqueda
     */
    public ClienteNoEncontradoExcepcion(String criterio, String valor) {
        super(String.format("El cliente con %s '%s' no fue encontrado", criterio, valor));
    }
}
