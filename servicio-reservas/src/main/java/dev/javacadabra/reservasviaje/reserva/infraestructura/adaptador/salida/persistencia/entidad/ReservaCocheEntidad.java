package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una reserva de coche en la base de datos.
 * Separada del agregado de dominio para mantener la arquitectura hexagonal.
 */
@Entity
@Table(name = "reserva_coche")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaCocheEntidad {

    @Id
    @Column(name = "reserva_id", nullable = false, unique = true, length = 100)
    private String reservaId;

    // Datos del coche
    @Column(name = "empresa_alquiler", nullable = false, length = 100)
    private String empresaAlquiler;

    @Column(name = "modelo_coche", nullable = false, length = 100)
    private String modeloCoche;

    @Column(name = "categoria_coche", nullable = false, length = 50)
    private String categoriaCoche;

    @Column(name = "ubicacion_recogida", nullable = false, length = 200)
    private String ubicacionRecogida;

    @Column(name = "ubicacion_devolucion", nullable = false, length = 200)
    private String ubicacionDevolucion;

    @Column(name = "fecha_recogida", nullable = false)
    private LocalDateTime fechaRecogida;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion;

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