package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando se intenta crear un cliente con un email
 * que ya está registrado en el sistema.
 *
 * <p>El email es único por cliente y se usa como credencial de acceso
 * al sistema, por lo que no pueden existir dos clientes con el mismo email.
 *
 * <p>Esta validación es crítica para:
 * <ul>
 *   <li>Seguridad del sistema de autenticación</li>
 *   <li>Unicidad de la cuenta de cliente</li>
 *   <li>Recuperación de contraseña</li>
 *   <li>Comunicaciones por email</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class EmailDuplicadoExcepcion extends ClienteDominioExcepcion {

    /**
     * Constructor con el email duplicado.
     *
     * @param email email que ya existe en el sistema
     */
    public EmailDuplicadoExcepcion(String email) {
        super(String.format(
                "Ya existe un cliente registrado con el email: %s. " +
                "Por favor, use otro email o recupere su cuenta existente",
                email
        ));
    }

    /**
     * Constructor con el email duplicado y el ID del cliente existente.
     *
     * @param email email que ya existe
     * @param clienteIdExistente ID del cliente que ya tiene ese email
     */
    public EmailDuplicadoExcepcion(String email, String clienteIdExistente) {
        super(String.format(
                "El email %s ya está registrado por el cliente %s. " +
                "Por favor, use otro email o recupere su cuenta existente",
                email,
                clienteIdExistente
        ));
    }
}
