package dev.javacadabra.reservasviaje.vuelo.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reserva_vuelo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaVueloJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String reservaViajeId;

    @Column(nullable = false)
    private String clienteId;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    private LocalDateTime fechaSalida;

    private String numeroVuelo;
    private String aerolinea;
    private String asiento;
    private String numeroReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReservaVueloEnum estado;

    private LocalDateTime fechaReserva;
    private LocalDateTime fechaCancelacion;
}

