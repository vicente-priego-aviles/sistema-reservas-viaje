package dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;

import java.time.LocalDateTime;

/**
 * Entidad que representa detalles adicionales de una reserva.
 * Contiene información complementaria y metadatos de la reserva.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
public class DetalleReserva {

    private Long id;
    private final String clienteId;
    private final String observaciones;
    private final String codigoConfirmacion;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String motivoCancelacion;

    /**
     * Constructor sin ID para nuevos detalles.
     */
    public DetalleReserva(
            String clienteId,
            String observaciones,
            String codigoConfirmacion) {

        this.id = null;
        this.clienteId = clienteId;
        this.observaciones = observaciones;
        this.codigoConfirmacion = codigoConfirmacion;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = null;
        this.motivoCancelacion = null;
    }

    /**
     * Actualiza las observaciones de la reserva.
     */
    public void actualizarObservaciones(String nuevasObservaciones) {
        // Las observaciones se manejan en el builder, este método es para futuras actualizaciones
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Registra el motivo de cancelación.
     */
    public void registrarCancelacion(String motivo) {
        if (this.motivoCancelacion != null) {
            throw new IllegalStateException("La reserva ya tiene un motivo de cancelación registrado");
        }
        this.motivoCancelacion = motivo;
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Verifica si la reserva fue cancelada.
     */
    public boolean fueCancelada() {
        return motivoCancelacion != null;
    }

    /**
     * Asigna un ID al detalle (usado por la capa de infraestructura).
     */
    public void asignarId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("El detalle ya tiene un ID asignado");
        }
        this.id = id;
    }

    /**
     * Marca la última modificación.
     */
    public void marcarModificacion() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
