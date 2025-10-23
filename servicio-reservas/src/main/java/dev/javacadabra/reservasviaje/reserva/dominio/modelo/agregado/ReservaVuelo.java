package dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.CancelacionNoPermitidaException;
import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.ReservaInvalidaException;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.Pasajero;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static ReservaVuelo crear(DatosVuelo datosVuelo,
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

    public static ReservaVuelo reconstruir(ReservaId reservaId,
                                           DatosVuelo datosVuelo,
                                           List<Pasajero> pasajeros,
                                           PrecioReserva precio,
                                           EstadoReserva estado,
                                           DetalleReserva detalleReserva,
                                           LocalDateTime fechaCreacion,
                                           LocalDateTime fechaModificacion) {
        return new ReservaVuelo(
                reservaId,
                datosVuelo,
                new ArrayList<>(pasajeros),
                precio,
                estado,
                detalleReserva,
                fechaCreacion,
                fechaModificacion
        );
    }

    public void asignarId(ReservaId id) {
        if (this.reservaId != null) throw new IllegalStateException("La reserva ya tiene un ID");
        this.reservaId = id;
    }

    private static void validarCreacion(DatosVuelo datosVuelo,
                                        List<Pasajero> pasajeros,
                                        PrecioReserva precio,
                                        DetalleReserva detalleReserva) {
        if (datosVuelo == null) throw ReservaInvalidaException.conMensaje("Datos del vuelo obligatorios");
        if (pasajeros == null || pasajeros.isEmpty()) throw ReservaInvalidaException.conMensaje("Debe haber al menos un pasajero");
        if (pasajeros.size() != datosVuelo.getNumeroPasajeros())
            throw ReservaInvalidaException.conMensaje("Número de pasajeros no coincide con el vuelo");
        if (precio == null || precio.esCero()) throw ReservaInvalidaException.porPrecioInvalido();
        if (detalleReserva == null) throw ReservaInvalidaException.conMensaje("Detalles de reserva obligatorios");
    }

    public void confirmar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.EN_PROCESO)
            throw ReservaInvalidaException.porEstadoInvalido(estado.name(), "PENDIENTE o EN_PROCESO");
        estado = EstadoReserva.CONFIRMADA;
        fechaModificacion = LocalDateTime.now();
        detalleReserva.marcarModificacion();
    }

    public void cancelar(String motivo) {
        if (!estado.permiteCancelacion())
            throw CancelacionNoPermitidaException.porEstado(reservaId, estado);
        estado = EstadoReserva.CANCELADA;
        fechaModificacion = LocalDateTime.now();
        detalleReserva.registrarCancelacion(motivo);
    }

    public void actualizarPrecio(PrecioReserva nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.esCero()) throw ReservaInvalidaException.porPrecioInvalido();
        precio = nuevoPrecio;
        fechaModificacion = LocalDateTime.now();
    }

    public List<Pasajero> getPasajeros() { return Collections.unmodifiableList(pasajeros); }
}
