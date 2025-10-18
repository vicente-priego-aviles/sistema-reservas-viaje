package dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.EstadoReservaHotel;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.HabitacionNumero;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.ReservaHotelId;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.TipoHabitacion;
import lombok.*;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AggregateRoot
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservaHotel {

    @Identity
    private ReservaHotelId id;
    private String reservaViajeId;
    private String clienteId;
    private String ciudad;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;

    // Detalles del hotel
    private String nombreHotel;
    private HabitacionNumero numeroHabitacion;
    private TipoHabitacion tipoHabitacion;
    private String numeroReserva;

    // Control
    private EstadoReservaHotel estado;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaCancelacion;

    public void reservar(String nombreHotel, HabitacionNumero numeroHabitacion,
                         TipoHabitacion tipoHabitacion) {
        if (this.estado == EstadoReservaHotel.RESERVADA) {
            throw new IllegalStateException("La reserva ya está confirmada");
        }

        this.nombreHotel = nombreHotel;
        this.numeroHabitacion = numeroHabitacion;
        this.tipoHabitacion = tipoHabitacion;
        this.numeroReserva = generarNumeroReserva();
        this.estado = EstadoReservaHotel.RESERVADA;
        this.fechaReserva = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.estado != EstadoReservaHotel.RESERVADA) {
            throw new IllegalStateException("Solo se pueden cancelar reservas confirmadas");
        }

        this.estado = EstadoReservaHotel.CANCELADA;
        this.fechaCancelacion = LocalDateTime.now();
    }

    private String generarNumeroReserva() {
        return "HTL-" + System.currentTimeMillis();
    }
}