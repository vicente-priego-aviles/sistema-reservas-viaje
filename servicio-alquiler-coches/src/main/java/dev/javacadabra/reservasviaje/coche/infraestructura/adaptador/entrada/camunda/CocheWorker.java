package dev.javacadabra.reservasviaje.coche.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.coche.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado.ReservaCoche;
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
public class CocheWorker {

    private final ReservarCochePuertoEntrada reservarCocheUseCase;
    private final CancelarCochePuertoEntrada cancelarCocheUseCase;

    @JobWorker(type = "reservar-coche")
    public Map<String, Object> reservarCoche(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");
        String destino = (String) job.getVariablesAsMap().get("destino");
        String fechaInicio = (String) job.getVariablesAsMap().get("fechaInicio");
        String fechaFin = (String) job.getVariablesAsMap().get("fechaFin");

        log.info("🔄 Worker: reservar-coche - Reserva: {} en {}", reservaId, destino);

        try {
            // Simular posible fallo (5% de probabilidad)
            if (Math.random() < 0.05) {
                log.warn("⚠️ Simulando fallo en reserva de coche");
                throw new ZeebeBpmnError("ERROR_RESERVA_COCHE",
                        "No hay vehículos disponibles");
            }

            ReservaCoche reserva = reservarCocheUseCase.reservarCoche(
                    reservaId, clienteId, destino, fechaInicio, fechaFin);

            log.info("✅ Coche reservado exitosamente: {}", reserva.getModelo());

            return Map.of(
                    "cocheReservado", true,
                    "modelo", reserva.getModelo(),
                    "matricula", reserva.getMatricula(),
                    "categoria", reserva.getCategoria().name(),
                    "puntoRecogida", reserva.getPuntoRecogida(),
                    "numeroReservaCoche", reserva.getNumeroReserva()
            );

        } catch (Exception e) {
            log.error("❌ Error al reservar coche: {}", e.getMessage());
            throw new ZeebeBpmnError("ERROR_RESERVA_COCHE", e.getMessage());
        }
    }

    @JobWorker(type = "cancelar-coche")
    public void cancelarCoche(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");

        log.info("🔄 Worker: cancelar-coche - Reserva: {}", reservaId);

        try {
            cancelarCocheUseCase.cancelarCoche(reservaId);
            log.info("✅ Coche cancelado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al cancelar coche: {}", e.getMessage());
        }
    }
}