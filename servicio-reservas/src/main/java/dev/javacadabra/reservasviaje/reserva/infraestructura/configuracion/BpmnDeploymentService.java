package dev.javacadabra.reservasviaje.reserva.infraestructura.configuracion;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para desplegar automáticamente procesos BPMN en Camunda 8 (Zeebe).
 *
 * <p>Se ejecuta automáticamente al iniciar la aplicación usando @PostConstruct.
 * Los archivos BPMN deben estar en src/main/resources/bpmn/
 *
 * <p>Orden de despliegue:
 * 1. Subprocesos (para que estén disponibles cuando se despliegue el proceso principal)
 * 2. Proceso principal
 *
 * @author JavaCadabra
 * @see ZeebeClient
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BpmnDeploymentService {

    private final ZeebeClient zeebeClient;

    /**
     * Orden de despliegue de procesos BPMN.
     * Los subprocesos DEBEN desplegarse primero.
     * proceso-principal.bpmn e iniciar-reserva.form se despliegan juntos en un
     * único batch (ver desplegarFormYProcesoPrincipal) para que Tasklist
     * Self-Managed pueda resolver la referencia del start event form.
     */
    private static final List<String> ORDEN_DESPLIEGUE = Arrays.asList(
            "bpmn/subproceso-gestion-cliente.bpmn",
            "bpmn/subproceso-reserva.bpmn",
            "bpmn/subproceso-pago.bpmn",
            "bpmn/forms/gestionar-reserva-vuelo.form",
            "bpmn/forms/gestionar-reserva-hotel.form",
            "bpmn/forms/gestionar-reserva-coche.form"
    );

    private static final int MAX_REINTENTOS = 3;
    private static final long TIEMPO_ESPERA_REINTENTO_MS = 5000; // 5 segundos

    /**
     * Despliega automáticamente los procesos BPMN al iniciar la aplicación.
     *
     * <p>Se ejecuta después de la inyección de dependencias.
     * Si falla, registra el error pero NO detiene la aplicación (permite que el servicio inicie).
     */
    @PostConstruct
    public void desplegarProcesos() {
        log.info("🚀 Iniciando despliegue de procesos BPMN en Camunda...");

        // Verificar conectividad con Zeebe antes de intentar desplegar
        if (!verificarConexionZeebe()) {
            log.error("❌ No se pudo conectar con Zeebe. Los procesos NO se desplegaron.");
            log.warn("⚠️  La aplicación continuará, pero los procesos BPMN no estarán disponibles.");
            return;
        }

        int procesosExitosos = 0;
        int procesosFallidos = 0;
        int total = ORDEN_DESPLIEGUE.size() + 1; // +1 por el batch form+proceso-principal

        for (String rutaProceso : ORDEN_DESPLIEGUE) {
            try {
                if (desplegarProcesoConReintentos(rutaProceso)) {
                    procesosExitosos++;
                } else {
                    procesosFallidos++;
                }
            } catch (Exception e) {
                log.error("❌ Error crítico al desplegar {}: {}", rutaProceso, e.getMessage(), e);
                procesosFallidos++;
            }
        }

        // Desplegar proceso-principal junto con su start event form en un único batch.
        // En Camunda 8 Self-Managed, la linked form del start event solo se resuelve
        // si form y BPMN pertenecen al mismo deployment.
        try {
            if (desplegarFormYProcesoPrincipalConReintentos()) {
                procesosExitosos++;
            } else {
                procesosFallidos++;
            }
        } catch (Exception e) {
            log.error("❌ Error crítico al desplegar batch form+proceso-principal: {}", e.getMessage(), e);
            procesosFallidos++;
        }

        // Resumen final
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📊 Resumen de despliegue:");
        log.info("   ✅ Exitosos: {}", procesosExitosos);
        log.info("   ❌ Fallidos:  {}", procesosFallidos);
        log.info("   📋 Total:     {}", total);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (procesosExitosos == total) {
            log.info("✅ Todos los procesos BPMN desplegados correctamente");
        } else if (procesosExitosos > 0) {
            log.warn("⚠️  Despliegue parcial: {}/{} procesos desplegados", procesosExitosos, total);
        } else {
            log.error("❌ Ningún proceso BPMN pudo ser desplegado");
        }
    }

    /**
     * Verifica que Zeebe esté accesible antes de intentar desplegar.
     *
     * @return true si Zeebe responde, false en caso contrario
     */
    private boolean verificarConexionZeebe() {
        log.info("🔍 Verificando conexión con Zeebe...");

        try {
            // Intentar obtener la topología del cluster como health check
            var topology = zeebeClient.newTopologyRequest()
                    .send()
                    .join();

            log.info("✅ Conexión con Zeebe establecida");
            log.debug("   Brokers: {}, Partitions: {}",
                    topology.getBrokers().size(),
                    topology.getPartitionsCount());

            return true;

        } catch (Exception e) {
            log.error("❌ No se pudo conectar con Zeebe: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Despliega un proceso BPMN con mecanismo de reintentos.
     *
     * @param rutaProceso Ruta del archivo BPMN (relativa a resources/)
     * @return true si el despliegue fue exitoso, false en caso contrario
     */
    private boolean desplegarProcesoConReintentos(String rutaProceso) {
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                desplegarProceso(rutaProceso);
                return true;

            } catch (Exception e) {
                if (intento < MAX_REINTENTOS) {
                    log.warn("⚠️  Intento {}/{} fallido para {}: {}. Reintentando en {}ms...",
                            intento, MAX_REINTENTOS, rutaProceso, e.getMessage(), TIEMPO_ESPERA_REINTENTO_MS);

                    try {
                        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_REINTENTO_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("❌ Reintentos interrumpidos para {}", rutaProceso);
                        return false;
                    }
                } else {
                    log.error("❌ Todos los intentos fallaron para {}: {}", rutaProceso, e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Despliega un único proceso BPMN en Zeebe.
     *
     * @param rutaProceso Ruta del archivo BPMN (relativa a resources/)
     * @throws IOException Si el archivo no se encuentra o no se puede leer
     */
    private void desplegarProceso(String rutaProceso) throws IOException {
        log.info("📋 Desplegando: {}", rutaProceso);

        // Usar PathMatchingResourcePatternResolver para mejor compatibilidad con JAR/Docker
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:" + rutaProceso);

        if (!resource.exists()) {
            log.warn("⚠️  Recurso no encontrado: {}", rutaProceso);
            throw new IOException("Archivo BPMN no encontrado: " + rutaProceso);
        }

        if (!resource.isReadable()) {
            log.error("❌ Recurso no legible: {}", rutaProceso);
            throw new IOException("Archivo BPMN no legible: " + rutaProceso);
        }

        // Desplegar en Zeebe
        DeploymentEvent deployment = zeebeClient.newDeployResourceCommand()
                .addResourceStream(resource.getInputStream(), resource.getFilename())
                .send()
                .join();

        // Loguear información detallada del despliegue
        logDetallesDespliegue(resource.getFilename(), deployment);
    }

    /**
     * Registra información detallada sobre el despliegue exitoso.
     *
     * @param nombreArchivo Nombre del archivo BPMN desplegado
     * @param deployment Evento de despliegue de Zeebe
     */
    private void logDetallesDespliegue(String nombreArchivo, DeploymentEvent deployment) {
        log.info("✅ Desplegado exitosamente: {}", nombreArchivo);
        log.info("   📌 Deployment Key: {}", deployment.getKey());

        // Información de procesos desplegados
        for (Process process : deployment.getProcesses()) {
            log.info("   🔧 Proceso: {} (ID: {}, Versión: {}, Key: {})",
                    process.getBpmnProcessId(),
                    process.getBpmnProcessId(),
                    process.getVersion(),
                    process.getProcessDefinitionKey());
        }
    }

    /**
     * Despliega iniciar-reserva.form y proceso-principal.bpmn en un único batch.
     * En Camunda 8 Self-Managed, la linked form del start event solo se resuelve
     * en Tasklist si form y proceso pertenecen al mismo deployment.
     */
    private boolean desplegarFormYProcesoPrincipalConReintentos() {
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                desplegarFormYProcesoPrincipal();
                return true;
            } catch (Exception e) {
                if (intento < MAX_REINTENTOS) {
                    log.warn("⚠️  Intento {}/{} fallido para batch form+proceso-principal: {}. Reintentando en {}ms...",
                            intento, MAX_REINTENTOS, e.getMessage(), TIEMPO_ESPERA_REINTENTO_MS);
                    try {
                        TimeUnit.MILLISECONDS.sleep(TIEMPO_ESPERA_REINTENTO_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("❌ Reintentos interrumpidos para batch form+proceso-principal");
                        return false;
                    }
                } else {
                    log.error("❌ Todos los intentos fallaron para batch form+proceso-principal: {}", e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    private void desplegarFormYProcesoPrincipal() throws IOException {
        log.info("📋 Desplegando batch: iniciar-reserva.form + proceso-principal.bpmn");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource form = resolver.getResource("classpath:bpmn/forms/iniciar-reserva.form");
        Resource bpmn = resolver.getResource("classpath:bpmn/proceso-principal.bpmn");

        if (!form.exists()) throw new IOException("Archivo no encontrado: bpmn/forms/iniciar-reserva.form");
        if (!bpmn.exists()) throw new IOException("Archivo no encontrado: bpmn/proceso-principal.bpmn");

        DeploymentEvent deployment = zeebeClient.newDeployResourceCommand()
                .addResourceStream(form.getInputStream(), form.getFilename())
                .addResourceStream(bpmn.getInputStream(), bpmn.getFilename())
                .send()
                .join();

        log.info("✅ Batch desplegado — key: {}", deployment.getKey());
        for (Process process : deployment.getProcesses()) {
            log.info("   🔧 Proceso: {} (versión: {}, key: {})",
                    process.getBpmnProcessId(), process.getVersion(), process.getProcessDefinitionKey());
        }
    }

    /**
     * Método auxiliar para desplegar todos los archivos BPMN de un directorio.
     * Útil para desarrollo/testing.
     *
     * @param directorioPatron Patrón del directorio (ej: "classpath:bpmn/*.bpmn")
     */
    public void desplegarDirectorio(String directorioPatron) {
        log.info("🔍 Buscando archivos BPMN en: {}", directorioPatron);

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] recursos = resolver.getResources(directorioPatron);

            log.info("📋 Encontrados {} archivos BPMN", recursos.length);

            for (Resource resource : recursos) {
                try {
                    DeploymentEvent deployment = zeebeClient.newDeployResourceCommand()
                            .addResourceStream(resource.getInputStream(), resource.getFilename())
                            .send()
                            .join();

                    logDetallesDespliegue(resource.getFilename(), deployment);

                } catch (Exception e) {
                    log.error("❌ Error al desplegar {}: {}", resource.getFilename(), e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("❌ Error al buscar archivos BPMN: {}", e.getMessage());
        }
    }
}