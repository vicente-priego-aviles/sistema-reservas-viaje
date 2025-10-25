package dev.javacadabra.reservasviaje.vuelo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Aplicación principal del Servicio de Vuelos.
 *
 * <p>Este microservicio gestiona:
 * <ul>
 *   <li>CRUD de Vuelos</li>
 *   <li>Validación de Vuelos</li>
 *   <li>Validación de ...</li>
 *   <li>Gestión de ...</li>
 * </ul>
 *
 * <p>Arquitectura: Hexagonal + DDD
 * <p>Puerto: reservasviaje
 *
 * @author javacadabra
 * @version 1.0.0
 */
@SpringBootApplication
@Slf4j
public class ServicioVuelosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioVuelosApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("");
        log.info("============================================");
        log.info("✈\uFE0F Servicio de Vuelos INICIADO");
        log.info("============================================");
        log.info("📊 Puerto: 9081");
        log.info("📖 Swagger UI: http://localhost:9081/swagger-ui.html");
        log.info("🔍 H2 Console: http://localhost:9081/h2-console");
        log.info("💚 Health: http://localhost:9081/actuator/health");
        log.info("============================================");
        log.info("");
    }
}
