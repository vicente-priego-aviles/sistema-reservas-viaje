package dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.CancelacionNoPermitidaException;
import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.ReservaInvalidaException;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.DatosHotel;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.EstadoReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.PrecioReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import lombok.*;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.LocalDateTime;

/**
 * Agregado raíz que representa una reserva de hotel.
 * Gestiona toda la lógica de negocio relacionada con reservas de hoteles.
 */
@AggregateRoot
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservaHotel {

    private ReservaId reservaId;
    private final DatosHotel datosHotel;
    private PrecioReserva precio;
    private EstadoReserva estado;
    private final DetalleReserva detalleReserva;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    /**
     * Crea una nueva reserva de hotel.
     * Factory method que garantiza la creación de un agregado válido.
     */
    public static ReservaHotel crear(
            DatosHotel datosHotel,
            PrecioReserva precio,
            DetalleReserva detalleReserva) {

        validarCreacion(datosHotel, precio, detalleReserva);

        return new ReservaHotel(
                ReservaId.generar(),
                datosHotel,
                precio,
                EstadoReserva.PENDIENTE,
                detalleReserva,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * Valida los datos para crear una reserva.
     */
    private static void validarCreacion(
            DatosHotel datosHotel,
            PrecioReserva precio,
            DetalleReserva detalleReserva) {

        if (datosHotel == null) {
            throw ReservaInvalidaException.conMensaje("Los datos del hotel son obligatorios");
        }

        if (precio == null || precio.esCero()) {
            throw ReservaInvalidaException.porPrecioInvalido();
        }

        if (detalleReserva == null) {
            throw ReservaInvalidaException.conMensaje("Los detalles de la reserva son obligatorios");
        }
    }

    /**
     * Confirma la reserva de hotel.
     */
    public void confirmar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.EN_PROCESO) {
            throw ReservaInvalidaException.porEstadoInvalido(
                    estado.name(),
                    "PENDIENTE o EN_PROCESO"
            );
        }

        this.estado = EstadoReserva.CONFIRMADA;
        this.fechaModificacion = LocalDateTime.now();
        this.detalleReserva.marcarModificacion();
    }

    /**
     * Marca la reserva como en proceso.
     */
    public void marcarEnProceso() {
        if (estado != EstadoReserva.PENDIENTE) {
            throw ReservaInvalidaException.porEstadoInvalido(
                    estado.name(),
                    "PENDIENTE"
            );
        }

        this.estado = EstadoReserva.EN_PROCESO;
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Cancela la reserva de hotel.
     */
    public void cancelar(String motivo) {
        if (!estado.permiteCancelacion()) {
            throw CancelacionNoPermitidaException.porEstado(reservaId, estado);
        }

        this.estado = EstadoReserva.CANCELADA;
        this.fechaModificacion = LocalDateTime.now();
        this.detalleReserva.registrarCancelacion(motivo);
    }

    /**
     * Marca la reserva como fallida.
     */
    public void marcarComoFallida(String motivo) {
        this.estado = EstadoReserva.FALLIDA;
        this.fechaModificacion = LocalDateTime.now();
        this.detalleReserva.registrarCancelacion(motivo);
    }

    /**
     * Marca la reserva como confirmada con advertencia.
     */
    public void confirmarConAdvertencia(String advertencia) {
        this.estado = EstadoReserva.CONFIRMADA_CON_ADVERTENCIA;
        this.fechaModificacion = LocalDateTime.now();
        this.detalleReserva.marcarModificacion();
    }

    /**
     * Actualiza el precio de la reserva.
     */
    public void actualizarPrecio(PrecioReserva nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.esCero()) {
            throw ReservaInvalidaException.porPrecioInvalido();
        }

        this.precio = nuevoPrecio;
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Verifica si la reserva está activa.
     */
    public boolean estaActiva() {
        return estado == EstadoReserva.CONFIRMADA ||
               estado == EstadoReserva.EN_PROCESO;
    }

    /**
     * Calcula el número de noches de la reserva.
     */
    public long calcularNoches() {
        return datosHotel.calcularNoches();
    }

    /**
     * Calcula el precio total basado en las noches.
     */
    public PrecioReserva calcularPrecioTotal() {
        return precio; // El precio ya incluye todas las noches
    }

    /**
     * Verifica si la reserva es para múltiples habitaciones.
     */
    public boolean esReservaMultiple() {
        return datosHotel.getNumeroHabitaciones() > 1;
    }

    /**
     * Asigna un ID a la reserva (usado por la capa de infraestructura).
     */
    public void asignarId(ReservaId id) {
        if (this.reservaId != null) {
            throw new IllegalStateException("La reserva ya tiene un ID asignado");
        }
        this.reservaId = id;
    }
}
