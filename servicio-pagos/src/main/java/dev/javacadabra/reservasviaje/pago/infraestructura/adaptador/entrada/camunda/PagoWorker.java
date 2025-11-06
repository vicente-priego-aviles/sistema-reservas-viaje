package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import dev.javacadabra.reservasviaje.pago.dominio.excepcion.MontoExcedeLimiteException;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PagoWorker {

    private final ProcesarPagoPuertoEntrada procesarPagoUseCase;
    private final ConfirmarReservaPuertoEntrada confirmarReservaUseCase;
    private final RevertirPagoPuertoEntrada revertirPagoUseCase;
    private final MarcarReservaAdvertenciaPuertoEntrada marcarAdvertenciaUseCase;

    @JobWorker(type = "procesar-pago")
    public Map<String, Object> procesarPago(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");

        Double montoVuelo = ((Number) job.getVariablesAsMap().get("precioVueloFinal")).doubleValue();
        Double montoHotel = ((Number) job.getVariablesAsMap().get("precioHotelFinal")).doubleValue();
        Double montoCoche = ((Number) job.getVariablesAsMap().get("precioCocheFinal")).doubleValue();
        Double monto = montoVuelo + montoHotel + montoCoche;

        log.info("🔄 Worker: procesar-pago - Reserva: {} - Monto: {}€", reservaId, monto);

        try {
            Pago pago = procesarPagoUseCase.procesarPago(reservaId, clienteId, monto);

            log.info("✅ Pago procesado - Transacción: {}", pago.getNumeroTransaccion());

            return Map.of(
                    "pagoRealizado", true,
                    "numeroTransaccion", pago.getNumeroTransaccion()
            );

        } catch (MontoExcedeLimiteException e) {
            log.error("❌ Monto excede límite: {}€", monto);
            throw new ZeebeBpmnError("ERROR_PROCESAR_PAGO", e.getMessage(), Map.of());

        } catch (Exception e) {
            log.error("❌ Error al procesar pago: {}", e.getMessage());
            throw new ZeebeBpmnError("ERROR_PROCESAR_PAGO", e.getMessage(), Map.of());
        }
    }

    @JobWorker(type = "confirmar-reserva")
    public Map<String, Object> confirmarReserva(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");

        log.info("🔄 Worker: confirmar-reserva - Reserva: {}", reservaId);

        try {
            String numeroConfirmacion = confirmarReservaUseCase.confirmarReserva(reservaId);

            log.info("✅ Reserva confirmada - Número: {}", numeroConfirmacion);

            return Map.of(
                    "reservaConfirmada", true,
                    "numeroConfirmacion", numeroConfirmacion
            );

        } catch (Exception e) {
            log.error("❌ Error al confirmar reserva: {}", e.getMessage());
            throw new ZeebeBpmnError("ERROR_CONFIRMAR_RESERVA", e.getMessage(), Map.of());
        }
    }

    @JobWorker(type = "revertir-estado-cliente")
    public void revertirEstadoCliente(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");
        String motivoReversion = (String) job.getVariablesAsMap()
                .getOrDefault("motivoReversion", "Error en proceso");

        log.info("🔄 Worker: revertir-estado-cliente - Reserva: {}", reservaId);

        try {
            // Aquí normalmente llamarías al servicio de clientes
            // Por ahora solo revertimos el pago
            revertirPagoUseCase.revertirPago(reservaId, motivoReversion);

            log.info("✅ Estado del cliente revertido");

        } catch (Exception e) {
            log.error("❌ Error al revertir estado: {}", e.getMessage());
            // No lanzamos error, queremos que continúe
        }
    }

    @JobWorker(type = "marcar-reserva-advertencia")
    public void marcarReservaAdvertencia(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");

        log.info("🔄 Worker: marcar-reserva-advertencia - Reserva: {}", reservaId);

        try {
            marcarAdvertenciaUseCase.marcarConAdvertencia(
                    reservaId,
                    "CONFIRMADA_CON_ADVERTENCIA",
                    true
            );

            log.info("⚠️ Reserva marcada con advertencia");

        } catch (Exception e) {
            log.error("❌ Error al marcar advertencia: {}", e.getMessage());
        }
    }
}
