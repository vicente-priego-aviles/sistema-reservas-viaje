package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando se intenta acceder a una tarjeta
 * que no existe en el agregado Cliente.
 *
 * <p>Esto puede ocurrir cuando:
 * <ul>
 *   <li>Se intenta obtener una tarjeta con un ID inexistente</li>
 *   <li>Se intenta eliminar una tarjeta que ya fue eliminada</li>
 *   <li>Se intenta actualizar una tarjeta que no pertenece al cliente</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class TarjetaNoEncontradaExcepcion extends ClienteDominioExcepcion {

    /**
     * Constructor con el ID de la tarjeta no encontrada.
     *
     * @param tarjetaId identificador de la tarjeta
     */
    public TarjetaNoEncontradaExcepcion(String tarjetaId) {
        super(String.format("La tarjeta con ID %s no fue encontrada", tarjetaId));
    }

    /**
     * Constructor con el ID de la tarjeta y el ID del cliente.
     *
     * @param tarjetaId identificador de la tarjeta
     * @param clienteId identificador del cliente
     */
    public TarjetaNoEncontradaExcepcion(String tarjetaId, String clienteId) {
        super(String.format(
                "La tarjeta con ID %s no fue encontrada para el cliente %s",
                tarjetaId,
                clienteId
        ));
    }
}
