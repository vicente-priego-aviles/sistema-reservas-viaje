package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio que se publica cuando un cliente inicia un proceso de reserva.
 *
 * @param clienteId ID del cliente que inicia la reserva
 * @param reservaId ID de la reserva iniciada
 * @param estadoAnterior Estado anterior del cliente
 * @param estadoCliente Estado nuevo del cliente (EN_PROCESO_RESERVA)
 * @param fechaInicio Fecha y hora en que se inició el proceso
 */
@DomainEvent
public record ProcesoReservaIniciadoEvento(
        String clienteId,
        String reservaId,
        String estadoAnterior,
        String estadoCliente,
        LocalDateTime fechaInicio
) {
}
