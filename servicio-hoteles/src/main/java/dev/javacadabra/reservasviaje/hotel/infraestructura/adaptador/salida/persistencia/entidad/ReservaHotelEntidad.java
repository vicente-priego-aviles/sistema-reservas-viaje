package dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reserva_hotel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaHotelEntidad {

    @Id
    private String id;

    @Column(nullable = false)
    private String reservaViajeId;

    @Column(nullable = false)
    private String clienteId;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private LocalDate fechaEntrada;

    @Column(nullable = false)
    private LocalDate fechaSalida;

    // Detalles del hotel
    private String nombreHotel;
    private String numeroHabitacion;

    @Enumerated(EnumType.STRING)
    private TipoHabitacionEnum tipoHabitacion;

    private String numeroReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReservaHotelEnum estado;

    private LocalDateTime fechaReserva;
    private LocalDateTime fechaCancelacion;
}