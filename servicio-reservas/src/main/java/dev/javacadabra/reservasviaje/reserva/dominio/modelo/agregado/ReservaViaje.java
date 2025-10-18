// ============================================
// 🟢 DOMINIO - ReservaViaje.java (Agregado Raíz)
// ============================================
package dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import lombok.*;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AggregateRoot
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservaViaje {

    @Identity
    private ReservaId id;
    private String clienteId;
    private String origen;
    private String destino;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double monto;

    // Detalles de reservas
    private DetalleVuelo detalleVuelo;
    private DetalleHotel detalleHotel;
    private DetalleCoche detalleCoche;

    // Control de proceso
    private EstadoReserva estado;
    private Long processInstanceKey;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaConfirmacion;
    private String numeroConfirmacion;

    // Métodos de negocio
    public void iniciarProceso(Long processInstanceKey) {
        this.estado = EstadoReserva.INICIADA;
        this.processInstanceKey = processInstanceKey;
        this.fechaCreacion = LocalDateTime.now();
    }

    public void confirmarVuelo(DetalleVuelo detalle) {
        this.detalleVuelo = detalle;
        actualizarEstado();
    }

    public void confirmarHotel(DetalleHotel detalle) {
        this.detalleHotel = detalle;
        actualizarEstado();
    }

    public void confirmarCoche(DetalleCoche detalle) {
        this.detalleCoche = detalle;
        actualizarEstado();
    }

    public void completar(String numeroConfirmacion) {
        this.estado = EstadoReserva.CONFIRMADA;
        this.numeroConfirmacion = numeroConfirmacion;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    public void fallar(String motivo) {
        this.estado = EstadoReserva.FALLIDA;
    }

    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    private void actualizarEstado() {
        if (detalleVuelo != null && detalleHotel != null && detalleCoche != null) {
            this.estado = EstadoReserva.EN_PROCESO_PAGO;
        } else {
            this.estado = EstadoReserva.EN_PROCESO_RESERVAS;
        }
    }

    public boolean estaCompleta() {
        return estado == EstadoReserva.CONFIRMADA;
    }
}
