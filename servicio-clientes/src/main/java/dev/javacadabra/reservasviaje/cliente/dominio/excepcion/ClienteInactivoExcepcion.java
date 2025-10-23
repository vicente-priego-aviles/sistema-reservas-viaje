package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando se intenta realizar una operación
 * con un cliente que está en estado INACTIVO.
 *
 * <p>Un cliente inactivo no puede:
 * <ul>
 *   <li>Realizar reservas de viajes</li>
 *   <li>Actualizar sus datos personales</li>
 *   <li>Agregar o eliminar tarjetas de crédito</li>
 * </ul>
 *
 * <p>Para reactivar un cliente, debe cambiar su estado a ACTIVO.
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class ClienteInactivoExcepcion extends ClienteDominioExcepcion {

    /**
     * Constructor con el ID del cliente inactivo.
     *
     * @param clienteId identificador del cliente
     */
    public ClienteInactivoExcepcion(String clienteId) {
        super(String.format(
                "El cliente %s está inactivo y no puede realizar esta operación. " +
                "Debe reactivar su cuenta primero",
                clienteId
        ));
    }

    /**
     * Constructor con el ID del cliente y la operación intentada.
     *
     * @param clienteId identificador del cliente
     * @param operacion operación que se intentó realizar
     */
    public ClienteInactivoExcepcion(String clienteId, String operacion) {
        super(String.format(
                "El cliente %s está inactivo y no puede realizar la operación: %s. " +
                "Debe reactivar su cuenta primero",
                clienteId,
                operacion
        ));
    }
}
