package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.TarjetaId;

/**
 * Excepción lanzada cuando una tarjeta no es encontrada en el sistema.
 *
 * <p>Esta excepción indica que se intentó acceder a una tarjeta
 * que no existe en el repositorio o no está asociada al cliente.
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class TarjetaNoEncontradaExcepcion extends ClienteDominioExcepcion {

    private final String tarjetaId;

    /**
     * Constructor con ID de la tarjeta.
     *
     * @param tarjetaId ID de la tarjeta no encontrada
     */
    public TarjetaNoEncontradaExcepcion(TarjetaId tarjetaId) {
        super(String.format("Tarjeta no encontrada con ID: %s", tarjetaId.valor()));
        this.tarjetaId = tarjetaId.valor();
    }

    /**
     * Constructor con ID de la tarjeta como String.
     *
     * @param tarjetaId ID de la tarjeta no encontrada
     */
    public TarjetaNoEncontradaExcepcion(String tarjetaId) {
        super(String.format("Tarjeta no encontrada con ID: %s", tarjetaId));
        this.tarjetaId = tarjetaId;
    }

    /**
     * Obtiene el ID de la tarjeta no encontrada.
     *
     * @return ID de la tarjeta
     */
    public String getTarjetaId() {
        return tarjetaId;
    }
}

