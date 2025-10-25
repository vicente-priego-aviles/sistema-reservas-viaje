package dev.javacadabra.reservasviaje.hotel;

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
public class ServicioHotelesAplicacion {

    public static void main(String[] args) {
        SpringApplication.run(ServicioHotelesAplicacion.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("");
        log.info("============================================");
        log.info("\uD83C\uDFE8 Servicio de Hoteles INICIADO");
        log.info("============================================");
        log.info("📊 Puerto: 9082");
        log.info("📖 Swagger UI: http://localhost:9082/swagger-ui.html");
        log.info("🔍 H2 Console: http://localhost:9082/h2-console");
        log.info("💚 Health: http://localhost:9082/actuator/health");
        log.info("============================================");
        log.info("");
    }
}
