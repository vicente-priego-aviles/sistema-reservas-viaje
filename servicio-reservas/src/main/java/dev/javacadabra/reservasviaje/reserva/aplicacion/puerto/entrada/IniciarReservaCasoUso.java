package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.IniciarReservaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.IniciarReservaRespuestaDTO;

public interface IniciarReservaCasoUso {

    IniciarReservaRespuestaDTO ejecutar(IniciarReservaDTO dto);
}
