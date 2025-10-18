package dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada;

public interface MarcarReservaAdvertenciaPuertoEntrada {
    void marcarConAdvertencia(
            String reservaViajeId,
            String estadoFinal,
            boolean requiereIntervencionManual
    );
}
