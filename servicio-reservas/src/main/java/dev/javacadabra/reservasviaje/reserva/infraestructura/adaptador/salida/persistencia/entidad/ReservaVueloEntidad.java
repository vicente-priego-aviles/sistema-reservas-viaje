package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una reserva de vuelo en la base de datos.
 * Separada del agregado de dominio para mantener la arquitectura hexagonal.
 */
@Entity
@Table(name = "reserva_vuelo")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaVueloEntidad {

    //@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    //private Long id;

    @Id
    @Column(name = "reserva_id", nullable = false, unique = true, length = 100)
    private String reservaId;

    // Datos del vuelo
    @Column(name = "numero_vuelo", nullable = false, length = 20)
    private String numeroVuelo;

    @Column(name = "aerolinea", nullable = false, length = 100)
    private String aerolinea;

    @Column(name = "origen", nullable = false, length = 100)
    private String origen;

    @Column(name = "destino", nullable = false, length = 100)
    private String destino;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDateTime fechaSalida;

    @Column(name = "fecha_llegada", nullable = false)
    private LocalDateTime fechaLlegada;

    @Column(name = "clase", nullable = false, length = 50)
    private String clase;

    @Column(name = "numero_pasajeros", nullable = false)
    private Integer numeroPasajeros;

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

    // Relación con pasajeros (One-to-Many)
    @OneToMany(
            mappedBy = "reservaVuelo",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<PasajeroEntidad> pasajeros = new ArrayList<>();

    /**
     * Añade un pasajero a la reserva.
     * Mantiene la bidireccionalidad de la relación.
     */
    public void agregarPasajero(PasajeroEntidad pasajero) {
        pasajeros.add(pasajero);
        pasajero.setReservaVuelo(this);
    }

    /**
     * Elimina un pasajero de la reserva.
     */
    public void eliminarPasajero(PasajeroEntidad pasajero) {
        pasajeros.remove(pasajero);
        pasajero.setReservaVuelo(null);
    }

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
