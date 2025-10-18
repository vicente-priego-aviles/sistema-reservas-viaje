package dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.salida;

import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.ReservaVueloId;

import java.util.Optional;

public interface ReservaVueloRepositorioPuertoSalida {

    ReservaVuelo guardar(ReservaVuelo reserva);

    Optional<ReservaVuelo> buscarPorId(ReservaVueloId id);

    Optional<ReservaVuelo> buscarPorReservaViajeId(String reservaViajeId);

    void eliminar(ReservaVueloId id);
}
