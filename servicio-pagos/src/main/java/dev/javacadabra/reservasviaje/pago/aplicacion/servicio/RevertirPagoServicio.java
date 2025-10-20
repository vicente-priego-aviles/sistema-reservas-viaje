package dev.javacadabra.reservasviaje.pago.aplicacion.servicio;

import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada.RevertirPagoPuertoEntrada;
import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.salida.PagoRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que implementa el caso de uso de revertir pago.
 *
 * <p>Este servicio implementa la compensación/reversión de pagos
 * cuando ocurre un error en el proceso de reserva.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevertirPagoServicio implements RevertirPagoPuertoEntrada {

    private final PagoRepositorioPuertoSalida repositorio;

    @Override
    @Transactional
    public void revertirPago(String reservaId, String motivo) {
        log.info("🔄 Iniciando reversión de pago para reservaViajeId: {} - Motivo: {}",
                reservaId, motivo);

        // Buscar el pago asociado a la reserva
        Pago pago = repositorio.buscarPorReservaViajeId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe pago para la reserva: " + reservaId
                ));

        // Invocar método del dominio para revertir
        try {
            pago.revertir();

            // Persistir el cambio de estado
            repositorio.guardar(pago);

            log.info("✅ Pago revertido exitosamente para reservaViajeId: {}", reservaId);

        } catch (IllegalStateException e) {
            log.warn("⚠️ No se pudo revertir el pago: {} - Estado actual: {}",
                    e.getMessage(), pago.getEstado());
            // No lanzamos excepción para que el proceso BPMN pueda continuar
        }
    }
}