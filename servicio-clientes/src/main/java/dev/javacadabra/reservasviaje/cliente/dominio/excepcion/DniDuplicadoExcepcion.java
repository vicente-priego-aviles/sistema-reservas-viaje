package dev.javacadabra.reservasviaje.cliente.dominio.excepcion;

/**
 * Excepción lanzada cuando se intenta crear un cliente con un DNI
 * que ya está registrado en el sistema.
 *
 * <p>El DNI es único por persona y no pueden existir dos clientes
 * con el mismo DNI registrado.
 *
 * <p>Esta validación es crítica para:
 * <ul>
 *   <li>Prevención de fraude</li>
 *   <li>Cumplimiento legal (KYC - Know Your Customer)</li>
 *   <li>Unicidad de identidad del cliente</li>
 *   <li>Protección de datos personales (RGPD)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
public class DniDuplicadoExcepcion extends ClienteDominioExcepcion {

    /**
     * Constructor con el DNI duplicado (enmascarado por seguridad).
     *
     * @param dniEnmascarado DNI enmascarado que ya existe
     */
    public DniDuplicadoExcepcion(String dniEnmascarado) {
        super(String.format(
                "Ya existe un cliente registrado con el DNI: %s. " +
                "Si ya tiene una cuenta, por favor inicie sesión. " +
                "Si cree que esto es un error, contacte con soporte",
                dniEnmascarado
        ));
    }

    /**
     * Constructor con el DNI duplicado y el ID del cliente existente.
     *
     * @param dniEnmascarado DNI enmascarado que ya existe
     * @param clienteIdExistente ID del cliente que ya tiene ese DNI
     */
    public DniDuplicadoExcepcion(String dniEnmascarado, String clienteIdExistente) {
        super(String.format(
                "El DNI %s ya está registrado por el cliente %s. " +
                "Si ya tiene una cuenta, por favor inicie sesión. " +
                "Si cree que esto es un error, contacte con soporte",
                dniEnmascarado,
                clienteIdExistente
        ));
    }
}
