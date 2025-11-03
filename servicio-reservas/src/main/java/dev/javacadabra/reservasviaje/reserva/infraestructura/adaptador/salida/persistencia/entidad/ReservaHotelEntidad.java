package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una reserva de hotel en la base de datos.
 * Separada del agregado de dominio para mantener la arquitectura hexagonal.
 */
@Entity
@Table(name = "reserva_hotel")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaHotelEntidad {

    @Id
    @Column(name = "reserva_id", nullable = false, unique = true, length = 100)
    private String reservaId;

    // Datos del hotel
    @Column(name = "nombre_hotel", nullable = false, length = 200)
    private String nombreHotel;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "direccion", nullable = false, length = 500)
    private String direccion;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "tipo_habitacion", nullable = false, length = 100)
    private String tipoHabitacion;

    @Column(name = "numero_habitaciones", nullable = false)
    private Integer numeroHabitaciones;

    @Column(name = "numero_huespedes", nullable = false)
    private Integer numeroHuespedes;

    // Precio
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "codigo_moneda", nullable = false, length = 3)
    private String codigoMoneda;

    // Estado
    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    // Detalle de reserva
    @Column(name = "cliente_id", nullable = false, length = 100)
    private String clienteId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "codigo_confirmacion", length = 50)
    private String codigoConfirmacion;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    // Fechas de auditoría
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
