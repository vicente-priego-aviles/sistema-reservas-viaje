package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando un cliente es bloqueado en el sistema.
 *
 * <p>Este evento se publica cuando un cliente pasa a estado BLOQUEADO
 * por motivos de seguridad, fraude detectado, tarjeta inválida o
 * violación de términos y condiciones.
 *
 * <p>Un cliente bloqueado no puede realizar ninguna operación hasta
 * que sea desbloqueado por un administrador.
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Alertar al cliente del bloqueo</li>
 *   <li>Servicio de seguridad: Registrar incidente de seguridad</li>
 *   <li>Servicio de auditoría: Documentar bloqueo para compliance</li>
 *   <li>Servicio de soporte: Crear ticket de revisión manual</li>
 *   <li>Proceso Camunda: Cancelar procesos activos del cliente</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@DomainEvent
@Getter
@ToString
@EqualsAndHashCode
public class ClienteBloqueadoEvento {

    /**
     * Identificador único del cliente bloqueado.
     */
    private final ClienteId clienteId;

    /**
     * Email del cliente bloqueado.
     */
    private final String email;

    /**
     * Motivo del bloqueo.
     */
    private final String motivoBloqueo;

    /**
     * Indica si el bloqueo requiere revisión manual.
     */
    private final boolean requiereRevisionManual;

    /**
     * Fecha y hora en que ocurrió el bloqueo.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de cliente bloqueado.
     *
     * @param clienteId identificador del cliente
     * @param email email del cliente
     * @param motivoBloqueo motivo del bloqueo
     * @param requiereRevisionManual si requiere revisión manual
     */
    public ClienteBloqueadoEvento(
            ClienteId clienteId,
            String email,
            String motivoBloqueo,
            boolean requiereRevisionManual
    ) {
        this.clienteId = clienteId;
        this.email = email;
        this.motivoBloqueo = motivoBloqueo;
        this.requiereRevisionManual = requiereRevisionManual;
        this.fechaEvento = LocalDateTime.now();
    }
}