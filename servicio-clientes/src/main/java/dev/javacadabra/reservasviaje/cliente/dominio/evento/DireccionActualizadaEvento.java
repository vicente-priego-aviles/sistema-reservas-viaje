package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando un cliente actualiza su dirección postal.
 *
 * <p>Este evento se publica después de que la dirección del cliente
 * ha sido actualizada exitosamente.
 *
 * <p>La dirección actualizada puede afectar a servicios como cálculo
 * de impuestos, disponibilidad de servicios por zona, etc.
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Confirmar cambio de dirección</li>
 *   <li>Servicio de auditoría: Registrar modificación de dirección</li>
 *   <li>Servicio de logística: Actualizar zona de entrega</li>
 *   <li>Servicio de facturación: Actualizar dirección fiscal</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@DomainEvent
@Getter
@ToString
@EqualsAndHashCode
public class DireccionActualizadaEvento {

    /**
     * Identificador único del cliente.
     */
    private final ClienteId clienteId;

    /**
     * Ciudad de la nueva dirección.
     */
    private final String ciudad;

    /**
     * Código postal de la nueva dirección.
     */
    private final String codigoPostal;

    /**
     * País de la nueva dirección.
     */
    private final String pais;

    /**
     * Dirección completa en formato resumido (ciudad, país).
     */
    private final String direccionResumida;

    /**
     * Fecha y hora en que se actualizó la dirección.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de dirección actualizada.
     *
     * @param clienteId identificador del cliente
     * @param ciudad ciudad de la dirección
     * @param codigoPostal código postal
     * @param pais país de la dirección
     * @param direccionResumida dirección resumida
     */
    public DireccionActualizadaEvento(
            ClienteId clienteId,
            String ciudad,
            String codigoPostal,
            String pais,
            String direccionResumida
    ) {
        this.clienteId = clienteId;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
        this.pais = pais;
        this.direccionResumida = direccionResumida;
        this.fechaEvento = LocalDateTime.now();
    }
}