package dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reserva_coche")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaCocheEntidad {

    @Id
    private String id;

    @Column(nullable = false)
    private String reservaViajeId;

    @Column(nullable = false)
    private String modeloCoche;

    @Column(nullable = false)
    private String marcaCoche;

    @Column(nullable = false)
    private String matricula;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private BigDecimal precioPorDia;

    @Column(nullable = false)
    private BigDecimal precioTotal;

    @Column(nullable = false)
    private String lugarRecogida;

    @Column(nullable = false)
    private String lugarDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReservaCocheEnum estado;

    private String observaciones;

    private LocalDateTime fechaReserva;
    private LocalDateTime fechaCancelacion;
}

enum EstadoReservaCocheEnum {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    COMPLETADA
}
