package dev.javacadabra.reservasviaje.vuelo.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.vuelo.dominio.excepcion.VueloNoDisponibleException;
import lombok.*;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDateTime;

@AggregateRoot
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservaVuelo {

    @Identity
    private ReservaVueloId id;
    private String reservaViajeId;
    private String clienteId;
    private String origen;
    private String destino;
    private LocalDateTime fechaSalida;

    // Detalles del vuelo
    private String numeroVuelo;
    private String aerolinea;
    private AsientoNumero asiento;
    private String numeroReserva;

    // Control
    private EstadoReservaVuelo estado;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaCancelacion;

    // Métodos de negocio
    public void reservar(String numeroVuelo, String aerolinea, AsientoNumero asiento) {
        if (this.estado == EstadoReservaVuelo.RESERVADA) {
            throw new IllegalStateException("La reserva ya está confirmada");
        }

        this.numeroVuelo = numeroVuelo;
        this.aerolinea = aerolinea;
        this.asiento = asiento;
        this.numeroReserva = generarNumeroReserva();
        this.estado = EstadoReservaVuelo.RESERVADA;
        this.fechaReserva = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.estado != EstadoReservaVuelo.RESERVADA) {
            throw new IllegalStateException("Solo se pueden cancelar Vuelos confirmadas");
        }

        this.estado = EstadoReservaVuelo.CANCELADA;
        this.fechaCancelacion = LocalDateTime.now();
    }

    private String generarNumeroReserva() {
        return "VUE-" + System.currentTimeMillis();
    }

    public boolean estaReservada() {
        return estado == EstadoReservaVuelo.RESERVADA;
    }
}


