package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando un cliente es activado en el sistema.
 *
 * <p>Este evento se publica cuando un cliente que estaba en estado
 * PENDIENTE_VALIDACION pasa a estado ACTIVO, lo que significa que
 * ha completado el proceso de validación (email, teléfono, datos).
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Enviar confirmación de activación</li>
 *   <li>Servicio de CRM: Marcar cliente como activo en marketing</li>
 *   <li>Servicio de analytics: Registrar conversión de registro a activo</li>
 *   <li>Proceso Camunda: Continuar flujo de onboarding del cliente</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@DomainEvent
@Getter
@ToString
@EqualsAndHashCode
public class ClienteActivadoEvento {

    /**
     * Identificador único del cliente activado.
     */
    private final ClienteId clienteId;

    /**
     * Email del cliente activado.
     */
    private final String email;

    /**
     * Nombre completo del cliente.
     */
    private final String nombreCompleto;

    /**
     * Fecha y hora en que ocurrió la activación.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de cliente activado.
     *
     * @param clienteId identificador del cliente
     * @param email email del cliente
     * @param nombreCompleto nombre completo del cliente
     */
    public ClienteActivadoEvento(
            ClienteId clienteId,
            String email,
            String nombreCompleto
    ) {
        this.clienteId = clienteId;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.fechaEvento = LocalDateTime.now();
    }
}