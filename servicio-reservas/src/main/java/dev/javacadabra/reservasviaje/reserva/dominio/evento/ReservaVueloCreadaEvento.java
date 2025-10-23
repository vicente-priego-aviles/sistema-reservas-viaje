package dev.javacadabra.reservasviaje.reserva.dominio.evento;

import lombok.Getter;
import org.jmolecules.event.annotation.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio que se dispara cuando se crea una reserva de vuelo.
 */
@DomainEvent
@Getter
public class ReservaVueloCreadaEvento {

    private final String reservaId;
    private final String clienteId;
    private final String numeroVuelo;
    private final LocalDateTime fechaSalida;
    private final BigDecimal precio;
    private final LocalDateTime fechaEvento;

    public ReservaVueloCreadaEvento(
            String reservaId,
            String clienteId,
            String numeroVuelo,
            LocalDateTime fechaSalida,
            BigDecimal precio) {

        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.numeroVuelo = numeroVuelo;
        this.fechaSalida = fechaSalida;
        this.precio = precio;
        this.fechaEvento = LocalDateTime.now();
    }
}
