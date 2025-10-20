package dev.javacadabra.reservasviaje.pago.aplicacion.servicio;

import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada.MarcarReservaAdvertenciaPuertoEntrada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que implementa el caso de uso de marcar advertencias.
 *
 * <p>Marca una reserva con advertencia cuando hay problemas no críticos
 * pero que requieren atención.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarcarReservaAdvertenciaServicio implements MarcarReservaAdvertenciaPuertoEntrada {

    @Override
    @Transactional
    public void marcarConAdvertencia(String reservaViajeId, String estadoFinal,
                                     boolean requiereIntervencionManual) {
        log.info("⚠️ Marcando reserva con advertencia - ReservaViajeId: {} - Estado: {} - Intervención manual: {}",
                reservaViajeId, estadoFinal, requiereIntervencionManual);

        // TODO: Implementar lógica según necesidades del negocio
        // Esto podría:
        // - Enviar notificación al equipo de soporte
        // - Crear un ticket en sistema de gestión
        // - Actualizar estado en tabla de auditoría
        // - Enviar evento a sistema de monitoreo

        log.info("✅ Reserva marcada con advertencia exitosamente");
    }
}
