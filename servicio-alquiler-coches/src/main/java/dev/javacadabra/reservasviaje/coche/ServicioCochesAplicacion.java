package dev.javacadabra.reservasviaje.coche;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Aplicación principal del Servicio de Hoteles.
 * 
 * <p>Este microservicio gestiona:
 * <ul>
 *   <li>CRUD de Hoteles</li>
 *   <li>Validación de Hoteles</li>
 *   <li>Validación de ...</li>
 *   <li>Gestión de ...</li>
 * </ul>
 * 
 * <p>Arquitectura: Hexagonal + DDD
 * <p>Puerto: 9090
 * 
 * @author javacadabra
 * @version 1.0.0
 */
@SpringBootApplication
@Slf4j
public class ServicioCochesAplicacion {

    public static void main(String[] args) {
        SpringApplication.run(ServicioCochesAplicacion.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("");
        log.info("============================================");
        log.info("\uD83D\uDE97 Servicio de Alquiler de Coches INICIADO");
        log.info("============================================");
        log.info("📊 Puerto: 9083");
        log.info("📖 Swagger UI: http://localhost:9083/swagger-ui.html");
        log.info("🔍 H2 Console: http://localhost:9083/h2-console");
        log.info("💚 Health: http://localhost:9083/actuator/health");
        log.info("============================================");
        log.info("");
    }
}
