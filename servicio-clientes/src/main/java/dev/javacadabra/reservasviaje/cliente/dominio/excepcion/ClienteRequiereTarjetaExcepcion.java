package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando un cliente intenta realizar una operación
 * que requiere tener al menos una tarjeta de crédito registrada,
 * pero no tiene ninguna.
 *
 * <p>Esta excepción se lanza en situaciones como:
 * <ul>
 *   <li>Intentar iniciar una reserva sin tarjetas registradas</li>
 *   <li>Intentar eliminar la única tarjeta que tiene el cliente</li>
 *   <li>Validar que el cliente tenga métodos de pago disponibles</li>
 * </ul>
 *
 * <p>Regla de negocio: Un cliente debe tener al menos 1 tarjeta
 * registrada en todo momento después de su creación inicial.
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class ClienteRequiereTarjetaExcepcion extends ClienteDominioExcepcion {

    /**
     * Constructor con el ID del cliente sin tarjetas.
     *
     * @param clienteId identificador del cliente
     */
    public ClienteRequiereTarjetaExcepcion(String clienteId) {
        super(String.format(
                "El cliente %s debe tener al menos una tarjeta de crédito registrada. " +
                "Por favor, agregue una tarjeta antes de continuar",
                clienteId
        ));
    }

    /**
     * Constructor para la operación de eliminación de última tarjeta.
     *
     * @param clienteId identificador del cliente
     * @param tarjetaId identificador de la tarjeta que se intentó eliminar
     */
    public ClienteRequiereTarjetaExcepcion(String clienteId, String tarjetaId) {
        super(String.format(
                "No se puede eliminar la tarjeta %s del cliente %s porque es su única tarjeta. " +
                "Un cliente debe tener al menos una tarjeta registrada en todo momento",
                tarjetaId,
                clienteId
        ));
    }
}
