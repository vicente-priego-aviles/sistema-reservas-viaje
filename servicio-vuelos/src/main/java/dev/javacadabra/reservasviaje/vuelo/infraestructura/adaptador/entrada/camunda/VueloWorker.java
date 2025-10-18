package dev.javacadabra.reservasviaje.vuelo.aplicacion;

import dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.salida.ReservaVueloRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.*;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class VueloWorker {

    private final ReservarVueloPuertoEntrada reservarVueloUseCase;
    private final CancelarVueloPuertoEntrada cancelarVueloUseCase;

    @JobWorker(type = "reservar-vuelo")
    public Map<String, Object> reservarVuelo(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");
        String origen = (String) job.getVariablesAsMap().get("origen");
        String destino = (String) job.getVariablesAsMap().get("destino");

        log.info("🔄 Worker: reservar-vuelo - Reserva: {} ({} -> {})",
                reservaId, origen, destino);

        try {
            // Simular posible fallo (5% de probabilidad)
            if (Math.random() < 0.05) {
                log.warn("⚠️ Simulando fallo en reserva de vuelo");
                throw new ZeebeBpmnError("ERROR_RESERVA_VUELO",
                        "No hay vuelos disponibles para esta ruta");
            }

            ReservaVuelo reserva = reservarVueloUseCase.reservarVuelo(
                    reservaId, clienteId, origen, destino);

            log.info("✅ Vuelo reservado exitosamente: {}", reserva.getNumeroVuelo());

            return Map.of(
                    "vueloReservado", true,
                    "numeroVuelo", reserva.getNumeroVuelo(),
                    "aerolinea", reserva.getAerolinea(),
                    "asiento", reserva.getAsiento().valor(),
                    "numeroReservaVuelo", reserva.getNumeroReserva()
            );

        } catch (Exception e) {
            log.error("❌ Error al reservar vuelo: {}", e.getMessage());
            throw new ZeebeBpmnError("ERROR_RESERVA_VUELO", e.getMessage());
        }
    }

    @JobWorker(type = "cancelar-vuelo")
    public void cancelarVuelo(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");

        log.info("🔄 Worker: cancelar-vuelo - Reserva: {}", reservaId);

        try {
            cancelarVueloUseCase.cancelarVuelo(reservaId);
            log.info("✅ Vuelo cancelado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al cancelar vuelo: {}", e.getMessage());
            // No lanzamos error, la compensación debe completarse
        }
    }
}
