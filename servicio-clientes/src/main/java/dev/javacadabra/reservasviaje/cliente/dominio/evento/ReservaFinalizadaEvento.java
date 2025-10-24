package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio que se publica cuando finaliza una reserva y el cliente vuelve a estado ACTIVO.
 *
 * @param clienteId ID del cliente
 * @param reservaId ID de la reserva finalizada
 * @param estadoAnterior Estado anterior del cliente
 * @param estadoNuevo Estado nuevo del cliente (ACTIVO)
 * @param fechaFinalizacion Fecha y hora de finalización
 */
@DomainEvent
public record ReservaFinalizadaEvento(
        Long clienteId,
        String reservaId,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaFinalizacion
) {
}