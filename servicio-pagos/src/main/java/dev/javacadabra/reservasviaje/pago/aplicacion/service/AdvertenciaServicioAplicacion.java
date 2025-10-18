package dev.javacadabra.reservasviaje.pago.aplicacion.service;

import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada.MarcarReservaAdvertenciaPuertoEntrada;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdvertenciaServicioAplicacion implements MarcarReservaAdvertenciaPuertoEntrada {

    @Override
    public void marcarConAdvertencia(
            String reservaViajeId,
            String estadoFinal,
            boolean requiereIntervencionManual) {

        log.info("⚠️ Marcando reserva {} con advertencia - Estado: {} - Intervención manual: {}",
                reservaViajeId, estadoFinal, requiereIntervencionManual);

        // Aquí se guardaría en una tabla de advertencias o se notificaría al equipo
        // Por ahora solo logueamos

        if (requiereIntervencionManual) {
            log.warn("🚨 ATENCIÓN: Reserva {} requiere intervención manual", reservaViajeId);
        }
    }
}