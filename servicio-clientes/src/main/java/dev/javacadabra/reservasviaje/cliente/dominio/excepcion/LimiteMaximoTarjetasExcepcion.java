package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;

/**
 * Excepción lanzada cuando un cliente intenta agregar más tarjetas
 * del límite permitido.
 *
 * <p>Por defecto, un cliente puede tener máximo 5 tarjetas activas
 * para evitar abusos y facilitar la gestión.
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class LimiteMaximoTarjetasExcepcion extends ClienteDominioExcepcion {

    private final String clienteId;
    private final int limiteMaximo;
    private final int tarjetasActuales;

    /**
     * Constructor con detalles del límite.
     *
     * @param clienteId ID del cliente
     * @param limiteMaximo límite máximo de tarjetas permitidas
     * @param tarjetasActuales cantidad actual de tarjetas del cliente
     */
    public LimiteMaximoTarjetasExcepcion(ClienteId clienteId, int limiteMaximo, int tarjetasActuales) {
        super(String.format(
                "El cliente %s ha alcanzado el límite máximo de tarjetas (%d). Tarjetas actuales: %d",
                clienteId.valor(), limiteMaximo, tarjetasActuales
        ));
        this.clienteId = clienteId.valor();
        this.limiteMaximo = limiteMaximo;
        this.tarjetasActuales = tarjetasActuales;
    }

    /**
     * Constructor con ID de cliente como String.
     *
     * @param clienteId ID del cliente
     * @param limiteMaximo límite máximo de tarjetas permitidas
     * @param tarjetasActuales cantidad actual de tarjetas del cliente
     */
    public LimiteMaximoTarjetasExcepcion(String clienteId, int limiteMaximo, int tarjetasActuales) {
        super(String.format(
                "El cliente %s ha alcanzado el límite máximo de tarjetas (%d). Tarjetas actuales: %d",
                clienteId, limiteMaximo, tarjetasActuales
        ));
        this.clienteId = clienteId;
        this.limiteMaximo = limiteMaximo;
        this.tarjetasActuales = tarjetasActuales;
    }

    /**
     * Obtiene el ID del cliente.
     *
     * @return ID del cliente
     */
    public String getClienteId() {
        return clienteId;
    }

    /**
     * Obtiene el límite máximo de tarjetas.
     *
     * @return límite máximo
     */
    public int getLimiteMaximo() {
        return limiteMaximo;
    }

    /**
     * Obtiene la cantidad actual de tarjetas del cliente.
     *
     * @return cantidad de tarjetas
     */
    public int getTarjetasActuales() {
        return tarjetasActuales;
    }
}

