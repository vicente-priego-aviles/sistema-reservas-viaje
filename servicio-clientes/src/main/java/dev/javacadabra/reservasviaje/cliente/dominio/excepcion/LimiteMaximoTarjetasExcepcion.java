package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando un cliente intenta agregar más tarjetas
 * de las permitidas por el sistema.
 *
 * <p>El límite máximo de tarjetas por cliente es 3.
 *
 * <p>Esta restricción existe por motivos de seguridad y para
 * simplificar la gestión de métodos de pago del cliente.
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class LimiteMaximoTarjetasExcepcion extends ClienteDominioExcepcion {

    private static final int LIMITE_MAXIMO = 3;

    /**
     * Constructor con el ID del cliente que excedió el límite.
     *
     * @param clienteId identificador del cliente
     */
    public LimiteMaximoTarjetasExcepcion(String clienteId) {
        super(String.format(
                "El cliente %s ha alcanzado el límite máximo de %d tarjetas",
                clienteId,
                LIMITE_MAXIMO
        ));
    }

    /**
     * Constructor con cantidad actual de tarjetas.
     *
     * @param clienteId identificador del cliente
     * @param cantidadActual cantidad actual de tarjetas del cliente
     */
    public LimiteMaximoTarjetasExcepcion(String clienteId, int cantidadActual) {
        super(String.format(
                "El cliente %s ha alcanzado el límite máximo de tarjetas. " +
                "Límite: %d, Actual: %d. Debe eliminar una tarjeta antes de agregar otra",
                clienteId,
                LIMITE_MAXIMO,
                cantidadActual
        ));
    }

    /**
     * Obtiene el límite máximo de tarjetas permitidas.
     *
     * @return límite máximo de tarjetas
     */
    public static int getLimiteMaximo() {
        return LIMITE_MAXIMO;
    }
}
