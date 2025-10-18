package dev.javacadabra.reservasviaje.pago.aplicacion.service;

import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.salida.PagoRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoServicioAplicacion implements
        ProcesarPagoPuertoEntrada,
        ConfirmarReservaPuertoEntrada,
        RevertirPagoPuertoEntrada {

    private final PagoRepositorioPuertoSalida repositorio;

    @Override
    @Transactional
    public Pago procesarPago(String reservaViajeId, String clienteId, Double montoValor) {
        log.info("💳 Procesando pago de {:.2f}€ para reserva: {}", montoValor, reservaViajeId);

        Monto monto = new Monto(montoValor);

        Pago pago = Pago.builder()
                .id(PagoId.generar())
                .reservaViajeId(reservaViajeId)
                .clienteId(clienteId)
                .monto(monto)
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .estado(EstadoPago.PENDIENTE)
                .build();

        try {
            // Procesar el pago
            pago.procesar();
            pago = repositorio.guardar(pago);

            log.info("✅ Pago procesado exitosamente - Transacción: {}",
                    pago.getNumeroTransaccion());

            return pago;

        } catch (Exception e) {
            log.error("❌ Error al procesar pago: {}", e.getMessage());
            pago.fallar(e.getMessage());
            repositorio.guardar(pago);
            throw e;
        }
    }

    @Override
    @Transactional
    public String confirmarReserva(String reservaViajeId) {
        log.info("🔍 Confirmando reserva: {}", reservaViajeId);

        Pago pago = repositorio.buscarPorReservaViajeId(reservaViajeId)
                .orElseThrow(() -> new PagoNoEncontradoException(reservaViajeId));

        String numeroConfirmacion = "CONF-" + System.currentTimeMillis();
        pago.confirmar(numeroConfirmacion);

        repositorio.guardar(pago);

        log.info("✅ Reserva confirmada - Número: {}", numeroConfirmacion);
        return numeroConfirmacion;
    }

    @Override
    @Transactional
    public void revertirPago(String reservaViajeId, String motivo) {
        log.info("🔄 Revirtiendo pago para reserva: {} - Motivo: {}", reservaViajeId, motivo);

        Pago pago = repositorio.buscarPorReservaViajeId(reservaViajeId)
                .orElseThrow(() -> new PagoNoEncontradoException(reservaViajeId));

        pago.revertir();
        repositorio.guardar(pago);

        log.info("✅ Pago revertido exitosamente");
    }
}

