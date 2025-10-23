package dev.javacadabra.reservasviaje.reserva.dominio.evento;

import lombok.Getter;
import org.jmolecules.event.annotation.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Evento de dominio que se dispara cuando se crea una reserva de hotel.
 */
@DomainEvent
@Getter
public class ReservaHotelCreadaEvento {

    private final String reservaId;
    private final String clienteId;
    private final String nombreHotel;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final BigDecimal precio;
    private final LocalDateTime fechaEvento;

    public ReservaHotelCreadaEvento(
            String reservaId,
            String clienteId,
            String nombreHotel,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            BigDecimal precio) {

        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.nombreHotel = nombreHotel;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.precio = precio;
        this.fechaEvento = LocalDateTime.now();
    }
}
