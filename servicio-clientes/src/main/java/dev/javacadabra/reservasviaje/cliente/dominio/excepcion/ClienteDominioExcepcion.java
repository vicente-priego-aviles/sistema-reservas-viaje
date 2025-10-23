package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción base del dominio de Cliente.
 *
 * <p>Todas las excepciones de negocio relacionadas con el agregado
 * Cliente deben heredar de esta clase.
 *
 * <p>Esta excepción es de tipo {@link RuntimeException}, por lo que
 * no requiere ser declarada en las firmas de los métodos (unchecked).
 *
 * <p>Excepciones que heredan de esta:
 * <ul>
 *   <li>{@link ClienteNoEncontradoExcepcion}</li>
 *   <li>{@link ClienteInactivoExcepcion}</li>
 *   <li>{@link ClienteBloqueadoExcepcion}</li>
 *   <li>{@link EmailDuplicadoExcepcion}</li>
 *   <li>{@link DniDuplicadoExcepcion}</li>
 *   <li>{@link LimiteMaximoTarjetasExcepcion}</li>
 *   <li>{@link TarjetaNoEncontradaExcepcion}</li>
 *   <li>{@link ClienteRequiereTarjetaExcepcion}</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class ClienteDominioExcepcion extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje descripción del error de negocio
     */
    public ClienteDominioExcepcion(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa raíz.
     *
     * @param mensaje descripción del error de negocio
     * @param causa excepción que causó este error
     */
    public ClienteDominioExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

