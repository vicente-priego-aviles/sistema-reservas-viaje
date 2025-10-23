package dev.javacadabra.reservasviaje.reserva;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Aplicación principal del Servicio de Reservas.
 * 
 * <p>Este microservicio gestiona:
 * <ul>
 *   <li>CRUD de Reservas</li>
 *   <li>Validación de Reservas</li>
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
public class ServicioReservasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioReservasApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("");
        log.info("============================================");
        log.info("🚀 Servicio de Reservas INICIADO");
        log.info("============================================");
        log.info("📊 Puerto: 9080");
        log.info("📖 Swagger UI: http://localhost:9090/swagger-ui.html");
        log.info("🔍 H2 Console: http://localhost:9080/h2-console");
        log.info("💚 Health: http://localhost:9080/actuator/health");
        log.info("============================================");
        log.info("");
    }
}
