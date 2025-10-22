package dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.CancelacionNoPermitidaException;
import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.ReservaInvalidaException;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.Pasajero;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.DatosVuelo;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.EstadoReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.PrecioReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agregado raíz que representa una reserva de vuelo.
 * Gestiona toda la lógica de negocio relacionada con reservas de vuelos.
 */
@AggregateRoot
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservaVuelo {

    private ReservaId reservaId;
    private final DatosVuelo datosVuelo;
    private final List<Pasajero> pasajeros;
    private PrecioReserva precio;
    private EstadoReserva estado;
    private final DetalleReserva detalleReserva;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    /**
     * Crea una nueva reserva de vuelo.
     * Factory method que garantiza la creación de un agregado válido.
     */
    public static ReservaVuelo crear(
            DatosVuelo datosVuelo,
            List<Pasajero> pasajeros,
            PrecioReserva precio,
            DetalleReserva detalleReserva) {

        validarCreacion(datosVuelo, pasajeros, precio, detalleReserva);

        return new ReservaVuelo(
                ReservaId.generar(),
                datosVuelo,
                new ArrayList<>(pasajeros),
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
            DatosVuelo datosVuelo,
            List<Pasajero> pasajeros,
            PrecioReserva precio,
            DetalleReserva detalleReserva) {

        if (datosVuelo == null) {
            throw ReservaInvalidaException.conMensaje("Los datos del vuelo son obligatorios");
        }

        if (pasajeros == null || pasajeros.isEmpty()) {
            throw ReservaInvalidaException.conMensaje("Debe haber al menos un pasajero");
        }

        if (pasajeros.size() != datosVuelo.getNumeroPasajeros()) {
            throw ReservaInvalidaException.conMensaje(
                    String.format("El número de pasajeros (%d) no coincide con los datos del vuelo (%d)",
                            pasajeros.size(), datosVuelo.getNumeroPasajeros())
            );
        }

        if (precio == null || precio.esCero()) {
            throw ReservaInvalidaException.porPrecioInvalido();
        }

        if (detalleReserva == null) {
            throw ReservaInvalidaException.conMensaje("Los detalles de la reserva son obligatorios");
        }
    }

    /**
     * Confirma la reserva de vuelo.
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
     * Cancela la reserva de vuelo.
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
     * Añade un pasajero a la reserva.
     * Solo permitido si la reserva está en estado PENDIENTE.
     */
    public void agregarPasajero(Pasajero pasajero) {
        if (estado != EstadoReserva.PENDIENTE) {
            throw ReservaInvalidaException.conMensaje(
                    "Solo se pueden agregar pasajeros a reservas en estado PENDIENTE"
            );
        }

        if (pasajeros.size() >= datosVuelo.getNumeroPasajeros()) {
            throw ReservaInvalidaException.conMensaje(
                    "No se pueden agregar más pasajeros. Límite alcanzado: " + datosVuelo.getNumeroPasajeros()
            );
        }

        this.pasajeros.add(pasajero);
        this.fechaModificacion = LocalDateTime.now();
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
     * Calcula el precio total de la reserva.
     */
    public PrecioReserva calcularPrecioTotal() {
        return precio; // El precio ya incluye todos los pasajeros
    }

    /**
     * Obtiene una copia inmutable de la lista de pasajeros.
     */
    public List<Pasajero> getPasajeros() {
        return Collections.unmodifiableList(pasajeros);
    }

    /**
     * Verifica si la reserva está activa.
     */
    public boolean estaActiva() {
        return estado == EstadoReserva.CONFIRMADA ||
               estado == EstadoReserva.EN_PROCESO;
    }

    /**
     * Verifica si todos los pasajeros son adultos.
     */
    public boolean todosLosPasajerosSonAdultos() {
        return pasajeros.stream()
                .noneMatch(Pasajero::esMenorDeEdad);
    }

    /**
     * Cuenta el número de menores de edad.
     */
    public long contarMenoresDeEdad() {
        return pasajeros.stream()
                .filter(Pasajero::esMenorDeEdad)
                .count();
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