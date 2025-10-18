package dev.javacadabra.reservasviaje.hotel.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado.ReservaHotel;
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
public class HotelWorker {

    private final ReservarHotelPuertoEntrada reservarHotelUseCase;
    private final CancelarHotelPuertoEntrada cancelarHotelUseCase;

    @JobWorker(type = "reservar-hotel")
    public Map<String, Object> reservarHotel(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");
        String destino = (String) job.getVariablesAsMap().get("destino");
        String fechaInicio = (String) job.getVariablesAsMap().get("fechaInicio");
        String fechaFin = (String) job.getVariablesAsMap().get("fechaFin");

        log.info("🔄 Worker: reservar-hotel - Reserva: {} en {}", reservaId, destino);

        try {
            // Simular posible fallo (5% de probabilidad)
            if (Math.random() < 0.05) {
                log.warn("⚠️ Simulando fallo en reserva de hotel");
                throw new ZeebeBpmnError("ERROR_RESERVA_HOTEL",
                        "No hay habitaciones disponibles");
            }

            ReservaHotel reserva = reservarHotelUseCase.reservarHotel(
                    reservaId, clienteId, destino, fechaInicio, fechaFin);

            log.info("✅ Hotel reservado exitosamente: {}", reserva.getNombreHotel());

            return Map.of(
                    "hotelReservado", true,
                    "nombreHotel", reserva.getNombreHotel(),
                    "numeroHabitacion", reserva.getNumeroHabitacion().valor(),
                    "tipoHabitacion", reserva.getTipoHabitacion().name(),
                    "numeroReservaHotel", reserva.getNumeroReserva()
            );

        } catch (Exception e) {
            log.error("❌ Error al reservar hotel: {}", e.getMessage());
            throw new ZeebeBpmnError("ERROR_RESERVA_HOTEL", e.getMessage());
        }
    }

    @JobWorker(type = "cancelar-hotel")
    public void cancelarHotel(ActivatedJob job) {
        String reservaId = (String) job.getVariablesAsMap().get("reservaId");

        log.info("🔄 Worker: cancelar-hotel - Reserva: {}", reservaId);

        try {
            cancelarHotelUseCase.cancelarHotel(reservaId);
            log.info("✅ Hotel cancelado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al cancelar hotel: {}", e.getMessage());
        }
    }
}
