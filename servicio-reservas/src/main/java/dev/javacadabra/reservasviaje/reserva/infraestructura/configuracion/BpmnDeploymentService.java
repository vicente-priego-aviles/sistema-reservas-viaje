package dev.javacadabra.reservasviaje.reserva.infraestructura.configuracion;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BpmnDeploymentService {

    private final ZeebeClient zeebeClient;
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void desplegarProcesos() {
        log.info("🚀 Desplegando procesos BPMN en Camunda...");

        try {
            // Orden correcto: subprocesos primero, proceso principal después
            desplegarRecurso("classpath:bpmn/subproceso-gestion-cliente.bpmn");
            desplegarRecurso("classpath:bpmn/subproceso-proceso-reserva.bpmn");
            desplegarRecurso("classpath:bpmn/subproceso-pago.bpmn");
            desplegarRecurso("classpath:bpmn/proceso-principal.bpmn");

            log.info("✅ Todos los procesos BPMN desplegados correctamente");

        } catch (Exception e) {
            log.error("❌ Error al desplegar procesos BPMN: {}", e.getMessage(), e);
            throw new RuntimeException("Error en despliegue de procesos BPMN", e);
        }
    }

    private void desplegarRecurso(String rutaRecurso) throws IOException {
        log.info("📋 Desplegando: {}", rutaRecurso);

        Resource resource = resourceLoader.getResource(rutaRecurso);

        if (!resource.exists()) {
            log.warn("⚠️  Recurso no encontrado: {}", rutaRecurso);
            return;
        }

        DeploymentEvent deployment = zeebeClient.newDeployResourceCommand()
                .addResourceStream(resource.getInputStream(), resource.getFilename())
                .send()
                .join();

        log.info("✅ Desplegado: {} - Key: {}",
                resource.getFilename(), deployment.getKey());
    }
}
