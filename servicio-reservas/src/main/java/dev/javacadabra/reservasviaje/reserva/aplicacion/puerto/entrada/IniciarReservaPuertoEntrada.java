package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.IniciarReservaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaIniciadaDTO;

public interface IniciarReservaPuertoEntrada {
    ReservaIniciadaDTO iniciarReserva(IniciarReservaDTO dto);
}
