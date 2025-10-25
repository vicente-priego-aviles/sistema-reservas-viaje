package dev.javacadabra.reservasviaje.pago.aplicacion.servicio;

import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada.ConfirmarReservaPuertoEntrada;
import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.salida.PagoRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que implementa el caso de uso de confirmar reserva.
 *
 * <p>Confirma la reserva después de un pago exitoso.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmarPagoservicio implements ConfirmarReservaPuertoEntrada {

    private final PagoRepositorioPuertoSalida repositorio;

    @Override
    @Transactional
    public String confirmarReserva(String reservaId) {
        log.info("📝 Confirmando reserva para reservaViajeId: {}", reservaId);

        // Buscar el pago asociado a la reserva
        Pago pago = repositorio.buscarPorReservaViajeId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe pago para la reserva: " + reservaId
                ));

        // Generar número de confirmación
        String numeroConfirmacion = generarNumeroConfirmacion();

        // Invocar método del dominio para confirmar
        pago.confirmar(numeroConfirmacion);

        // Persistir el cambio de estado
        repositorio.guardar(pago);

        log.info("✅ Reserva confirmada - Número: {}", numeroConfirmacion);
        return numeroConfirmacion;
    }

    /**
     * Genera un número de confirmación único.
     */
    private String generarNumeroConfirmacion() {
        return "CONF-" + System.currentTimeMillis();
    }
}
