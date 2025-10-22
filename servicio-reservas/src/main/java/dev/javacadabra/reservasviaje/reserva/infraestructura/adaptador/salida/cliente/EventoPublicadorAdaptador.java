package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.cliente;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.EventoPublicadorPuerto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador para publicar eventos de dominio.
 * Implementa el puerto de salida EventoPublicadorPuerto.
 *
 * NOTA: Esta implementación usa Spring ApplicationEventPublisher para eventos locales.
 * En producción, se puede reemplazar por un message broker (Kafka, RabbitMQ, etc.)
 * sin cambiar el código del dominio ni de la aplicación.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventoPublicadorAdaptador implements EventoPublicadorPuerto {

    private final ApplicationEventPublisher springEventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public <T> void publicar(T evento) {
        try {
            log.info("📨 Publicando evento: {}", evento.getClass().getSimpleName());
            log.debug("📋 Contenido del evento: {}", serializarEvento(evento));

            // Publicar evento usando Spring Events (local)
            springEventPublisher.publishEvent(evento);

            log.info("✅ Evento publicado correctamente: {}", evento.getClass().getSimpleName());

        } catch (Exception e) {
            log.error("❌ Error al publicar evento {}: {}",
                    evento.getClass().getSimpleName(),
                    e.getMessage(),
                    e);

            // En producción, considerar:
            // - Retry logic
            // - Dead letter queue
            // - Circuit breaker
            throw new RuntimeException("Error al publicar evento", e);
        }
    }

    @Override
    public <T> void publicar(String tema, T evento) {
        try {
            log.info("📨 Publicando evento en tema '{}': {}", tema, evento.getClass().getSimpleName());
            log.debug("📋 Contenido del evento: {}", serializarEvento(evento));

            // Para publicación con tema, se puede implementar:
            // - Kafka: kafkaTemplate.send(tema, evento)
            // - RabbitMQ: rabbitTemplate.convertAndSend(exchange, tema, evento)
            // - AWS SNS: snsClient.publish(topicArn, mensaje)

            // Por ahora, usar Spring Events con wrapper que incluye el tema
            EventoConTema<T> eventoConTema = new EventoConTema<>(tema, evento);
            springEventPublisher.publishEvent(eventoConTema);

            log.info("✅ Evento publicado correctamente en tema '{}': {}",
                    tema,
                    evento.getClass().getSimpleName());

        } catch (Exception e) {
            log.error("❌ Error al publicar evento en tema '{}': {}",
                    tema,
                    e.getMessage(),
                    e);
            throw new RuntimeException("Error al publicar evento con tema", e);
        }
    }

    /**
     * Serializa el evento a JSON para logging.
     */
    private <T> String serializarEvento(T evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException e) {
            log.warn("⚠️ No se pudo serializar el evento a JSON: {}", e.getMessage());
            return evento.toString();
        }
    }

    /**
     * Wrapper para eventos con tema específico.
     * Útil para implementaciones que necesitan routing de mensajes.
     */
    public record EventoConTema<T>(String tema, T evento) {}
}
