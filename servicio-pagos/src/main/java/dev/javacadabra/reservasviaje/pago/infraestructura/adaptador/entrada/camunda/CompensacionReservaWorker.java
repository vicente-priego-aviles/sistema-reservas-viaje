package dev.javacadabra.reservasviaje.pago.infraestructura.adaptador.entrada.camunda;

import io.camunda.client.CamundaClient;
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
public class CompensacionReservaWorker {

    private final CamundaClient camundaClient;

    @JobWorker(type = "publicar-compensacion-reserva", autoComplete = true)
    public void publicarCompensacion(ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String reservaId = (String) variables.get("reservaId");

        if (reservaId == null) {
            log.error("❌ reservaId es nulo, no se puede publicar mensaje de compensación");
            throw new ZeebeBpmnError("ERROR_COMPENSACION",
                    "Falta reservaId para publicar compensación",
                    Map.of("motivoInvalidez", "Error interno: reservaId no disponible"));
        }

        log.info("📨 Publicando mensaje compensar-reserva para reserva: {}", reservaId);

        camundaClient.newPublishMessageCommand()
                .messageName("compensar-reserva")
                .correlationKey(reservaId)
                .send()
                .join();

        log.info("✅ Mensaje compensar-reserva publicado para reserva: {}", reservaId);
    }
}
