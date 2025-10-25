package dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor;

import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Estados del ciclo de vida de un cliente en el sistema.
 *
 * <p>El estado del cliente determina qué operaciones puede realizar
 * y cómo el sistema interactúa con él durante los procesos de negocio.
 *
 * <p>Transiciones permitidas:
 * <ul>
 *   <li>PENDIENTE_VALIDACION → ACTIVO (tras validar email/teléfono)</li>
 *   <li>ACTIVO → EN_PROCESO_RESERVA (al iniciar una reserva)</li>
 *   <li>EN_PROCESO_RESERVA → RESERVA_CONFIRMADA (tras pago exitoso)</li>
 *   <li>RESERVA_CONFIRMADA → ACTIVO (tras finalizar proceso)</li>
 *   <li>ACTIVO → BLOQUEADO (tarjeta inválida, fraude detectado)</li>
 *   <li>BLOQUEADO → ACTIVO (tras revisión y aprobación)</li>
 *   <li>ACTIVO → INACTIVO (cliente solicita baja)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@ValueObject
@Getter
public enum EstadoCliente {

    /**
     * Cliente recién registrado, pendiente de validar email y teléfono.
     * No puede realizar Pagos hasta completar la validación.
     */
    PENDIENTE_VALIDACION("Pendiente de Validación"),

    /**
     * Cliente activo y validado. Puede realizar todas las operaciones
     * normales del sistema, incluyendo Pagos de viajes.
     */
    ACTIVO("Activo"),

    /**
     * Cliente tiene una reserva en proceso. Estado temporal durante
     * el flujo de reserva de viaje (gestión de cliente → reserva → pago).
     */
    EN_PROCESO_RESERVA("En Proceso de Reserva"),

    /**
     * Reserva confirmada exitosamente. Estado temporal tras completar
     * el pago hasta que el proceso finaliza y vuelve a ACTIVO.
     */
    RESERVA_CONFIRMADA("Reserva Confirmada"),

    /**
     * Cliente bloqueado por problemas de seguridad, tarjeta inválida
     * o detección de actividad sospechosa. No puede realizar Pagos.
     * Requiere intervención manual para reactivar.
     */
    BLOQUEADO("Bloqueado"),

    /**
     * Cliente dado de baja temporalmente. No puede realizar operaciones.
     * Puede reactivarse si el cliente lo solicita.
     */
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Verifica si el cliente puede realizar Pagos en este estado.
     *
     * @return true si puede realizar Pagos, false en caso contrario
     */
    public boolean puedeRealizarPagos() {
        return this == ACTIVO || this == EN_PROCESO_RESERVA;
    }

    /**
     * Verifica si el estado permite actualizar información del cliente.
     *
     * @return true si permite actualizaciones, false en caso contrario
     */
    public boolean permiteActualizaciones() {
        return this != INACTIVO;
    }

    /**
     * Verifica si el estado es terminal (requiere acción para cambiar).
     *
     * @return true si es un estado terminal, false en caso contrario
     */
    public boolean esEstadoTerminal() {
        return this == BLOQUEADO || this == INACTIVO;
    }
}
