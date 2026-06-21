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

    private String clienteId;

    private String ciudadRecogida;

    @Column(nullable = false)
    private String modeloCoche;

    private String marcaCoche;

    @Column(nullable = false)
    private String matricula;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    private BigDecimal precioPorDia;

    private BigDecimal precioTotal;

    @Column(nullable = false)
    private String lugarRecogida;

    @Column(nullable = false)
    private String lugarDevolucion;

    private String numeroReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReservaCocheEnum estado;

    @Enumerated(EnumType.STRING)
    private CategoriaCocheEnum categoriaCoche;

    private String observaciones;

    private LocalDateTime fechaReserva;
    private LocalDateTime fechaCancelacion;
}
