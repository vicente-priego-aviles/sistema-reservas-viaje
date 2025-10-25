package dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.CategoriaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.EstadoReservaCoche;

import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.ReservaCocheId;
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
public class ReservaCoche {

    @Identity
    private ReservaCocheId id;
    private String reservaViajeId;
    private String clienteId;
    private String ciudadRecogida;
    private LocalDate fechaRecogida;
    private LocalDate fechaDevolucion;

    // Detalles del coche
    private String modelo;
    private String matricula;
    private CategoriaCoche categoria;
    private String puntoRecogida;
    private String puntoDevolucion;
    private String numeroReserva;

    // Control
    private EstadoReservaCoche estado;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaCancelacion;

    public void reservar(String modelo, String matricula, CategoriaCoche categoria,
                         String puntoRecogida) {
        if (this.estado == EstadoReservaCoche.RESERVADA) {
            throw new IllegalStateException("La reserva ya está confirmada");
        }

        this.modelo = modelo;
        this.matricula = matricula;
        this.categoria = categoria;
        this.puntoRecogida = puntoRecogida;
        this.puntoDevolucion = puntoRecogida; // Por defecto mismo punto
        this.numeroReserva = generarNumeroReserva();
        this.estado = EstadoReservaCoche.RESERVADA;
        this.fechaReserva = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.estado != EstadoReservaCoche.RESERVADA) {
            throw new IllegalStateException("Solo se pueden cancelar Pagos confirmadas");
        }

        this.estado = EstadoReservaCoche.CANCELADA;
        this.fechaCancelacion = LocalDateTime.now();
    }

    private String generarNumeroReserva() {
        return "CAR-" + System.currentTimeMillis();
    }
}