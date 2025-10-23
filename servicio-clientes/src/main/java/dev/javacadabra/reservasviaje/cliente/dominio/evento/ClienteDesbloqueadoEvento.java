package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando un cliente bloqueado es desbloqueado.
 *
 * <p>Este evento se publica cuando un administrador revisa el caso de
 * un cliente bloqueado y decide desbloquearlo, permitiéndole volver
 * a realizar operaciones normales en el sistema.
 *
 * <p>El cliente pasa de estado BLOQUEADO a ACTIVO tras el desbloqueo.
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Informar al cliente del desbloqueo</li>
 *   <li>Servicio de auditoría: Registrar desbloqueo para compliance</li>
 *   <li>Servicio de soporte: Cerrar ticket de revisión</li>
 *   <li>Servicio de CRM: Actualizar estado del cliente</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@DomainEvent
@Getter
@ToString
@EqualsAndHashCode
public class ClienteDesbloqueadoEvento {

    /**
     * Identificador único del cliente desbloqueado.
     */
    private final ClienteId clienteId;

    /**
     * Email del cliente desbloqueado.
     */
    private final String email;

    /**
     * Motivo original del bloqueo.
     */
    private final String motivoBloqueoOriginal;

    /**
     * Motivo del desbloqueo (justificación de la decisión).
     */
    private final String motivoDesbloqueo;

    /**
     * Administrador que realizó el desbloqueo.
     */
    private final String administrador;

    /**
     * Fecha y hora en que ocurrió el desbloqueo.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de cliente desbloqueado.
     *
     * @param clienteId identificador del cliente
     * @param email email del cliente
     * @param motivoBloqueoOriginal motivo original del bloqueo
     * @param motivoDesbloqueo motivo del desbloqueo
     * @param administrador administrador que desbloqueó
     */
    public ClienteDesbloqueadoEvento(
            ClienteId clienteId,
            String email,
            String motivoBloqueoOriginal,
            String motivoDesbloqueo,
            String administrador
    ) {
        this.clienteId = clienteId;
        this.email = email;
        this.motivoBloqueoOriginal = motivoBloqueoOriginal;
        this.motivoDesbloqueo = motivoDesbloqueo;
        this.administrador = administrador;
        this.fechaEvento = LocalDateTime.now();
    }
}