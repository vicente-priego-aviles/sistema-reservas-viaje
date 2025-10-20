package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoEntidad {

    @Id
    private String id;

    @Column(nullable = false)
    private String reservaViajeId;

    @Column(nullable = false)
    private String clienteId;

    @Column(nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagoEnum metodoPago;

    private String numeroTransaccion;
    private String numeroConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPagoEnum estado;

    private LocalDateTime fechaProcesamiento;
    private String mensajeError;
}
