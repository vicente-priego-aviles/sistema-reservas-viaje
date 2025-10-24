package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio que se publica cuando se confirma una reserva de un cliente.
 *
 * @param clienteId ID del cliente
 * @param reservaId ID de la reserva confirmada
 * @param estadoAnterior Estado anterior del cliente
 * @param estadoNuevo Estado nuevo del cliente (RESERVA_CONFIRMADA)
 * @param fechaConfirmacion Fecha y hora de confirmación
 */
@DomainEvent
public record ReservaConfirmadaEvento(
        String clienteId,
        String reservaId,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaConfirmacion
) {
}