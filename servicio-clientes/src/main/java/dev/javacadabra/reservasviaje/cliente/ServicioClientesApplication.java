package dev.javacadabra.reservasviaje.cliente;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Aplicación principal del Servicio de Clientes.
 * 
 * <p>Este microservicio gestiona:
 * <ul>
 *   <li>CRUD de clientes</li>
 *   <li>Validación de clientes</li>
 *   <li>Validación de tarjetas de crédito</li>
 *   <li>Gestión de estados del cliente</li>
 * </ul>
 * 
 * <p>Arquitectura: Hexagonal + DDD
 * <p>Puerto: 9080
 * 
 * @author javacadabra
 * @version 1.0.0
 */
@SpringBootApplication
@Slf4j
public class ServicioClientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioClientesApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("");
        log.info("============================================");
        log.info("🚀 Servicio de Clientes INICIADO");
        log.info("============================================");
        log.info("📊 Puerto: 9080");
        log.info("📖 Swagger UI: http://localhost:9080/swagger-ui.html");
        log.info("🔍 H2 Console: http://localhost:9080/h2-console");
        log.info("💚 Health: http://localhost:9080/actuator/health");
        log.info("============================================");
        log.info("");
    }
}
