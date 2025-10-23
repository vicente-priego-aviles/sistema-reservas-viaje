package dev.javacadabra.reservasviaje.reserva.dominio.evento;

import lombok.Getter;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio que se dispara cuando se cancela una reserva de vuelo.
 */
@DomainEvent
@Getter
public class ReservaVueloCanceladaEvento {

    private final String reservaId;
    private final String clienteId;
    private final String motivo;
    private final LocalDateTime fechaEvento;

    public ReservaVueloCanceladaEvento(
            String reservaId,
            String clienteId,
            String motivo) {

        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.motivo = motivo;
        this.fechaEvento = LocalDateTime.now();
    }
}