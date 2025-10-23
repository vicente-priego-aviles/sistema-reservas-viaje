package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando se intenta realizar una operación
 * con un cliente que está en estado BLOQUEADO.
 *
 * <p>Un cliente bloqueado no puede realizar ninguna operación
 * hasta que sea desbloqueado por un administrador.
 *
 * <p>Motivos comunes de bloqueo:
 * <ul>
 *   <li>Tarjeta de crédito inválida detectada</li>
 *   <li>Actividad sospechosa o fraude detectado</li>
 *   <li>Múltiples intentos fallidos de pago</li>
 *   <li>Violación de términos y condiciones</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class ClienteBloqueadoExcepcion extends ClienteDominioExcepcion {

    /**
     * Constructor con el ID del cliente bloqueado.
     *
     * @param clienteId identificador del cliente
     */
    public ClienteBloqueadoExcepcion(String clienteId) {
        super(String.format(
                "El cliente %s está bloqueado por motivos de seguridad. " +
                "Contacte con el servicio de atención al cliente",
                clienteId
        ));
    }

    /**
     * Constructor con el ID del cliente y el motivo del bloqueo.
     *
     * @param clienteId identificador del cliente
     * @param motivo motivo del bloqueo
     */
    public ClienteBloqueadoExcepcion(String clienteId, String motivo) {
        super(String.format(
                "El cliente %s está bloqueado. Motivo: %s. " +
                "Contacte con el servicio de atención al cliente",
                clienteId,
                motivo
        ));
    }
}
