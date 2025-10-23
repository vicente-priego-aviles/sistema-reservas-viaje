package dev.javacadabra.reservasviaje.reserva.dominio.evento;

import lombok.Getter;
import org.jmolecules.event.annotation.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio que se dispara cuando se crea una reserva de coche.
 */
@DomainEvent
@Getter
public class ReservaCocheCreadaEvento {

    private final String reservaId;
    private final String clienteId;
    private final String modeloCoche;
    private final LocalDateTime fechaRecogida;
    private final LocalDateTime fechaDevolucion;
    private final BigDecimal precio;
    private final LocalDateTime fechaEvento;

    public ReservaCocheCreadaEvento(
            String reservaId,
            String clienteId,
            String modeloCoche,
            LocalDateTime fechaRecogida,
            LocalDateTime fechaDevolucion,
            BigDecimal precio) {

        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.modeloCoche = modeloCoche;
        this.fechaRecogida = fechaRecogida;
        this.fechaDevolucion = fechaDevolucion;
        this.precio = precio;
        this.fechaEvento = LocalDateTime.now();
    }
}
