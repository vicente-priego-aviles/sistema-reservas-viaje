package dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.agregado.ReservaVuelo;

public interface ReservarVueloPuertoEntrada {

    ReservaVuelo reservarVuelo(String reservaViajeId, String clienteId,
                               String origen, String destino);
}
