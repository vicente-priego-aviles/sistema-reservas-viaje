package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.TarjetaId;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.TipoTarjeta;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando se elimina una tarjeta de crédito de un cliente.
 *
 * <p>Este evento se publica después de que una tarjeta ha sido eliminada
 * exitosamente del cliente. La eliminación solo es posible si el cliente
 * tiene más de una tarjeta (debe mantener al menos 1 tarjeta siempre).
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Confirmar al cliente la eliminación</li>
 *   <li>Servicio de seguridad: Notificar eliminación de método de pago</li>
 *   <li>Servicio de auditoría: Registrar cambio en métodos de pago</li>
 *   <li>Servicio de analytics: Analizar comportamiento de clientes</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@DomainEvent
@Getter
@ToString
@EqualsAndHashCode
public class TarjetaEliminadaEvento {

    /**
     * Identificador único del cliente.
     */
    private final ClienteId clienteId;

    /**
     * Identificador único de la tarjeta eliminada.
     */
    private final TarjetaId tarjetaId;

    /**
     * Tipo de tarjeta eliminada (Visa, Mastercard, Amex, etc.).
     */
    private final TipoTarjeta tipoTarjeta;

    /**
     * Últimos 4 dígitos de la tarjeta eliminada.
     */
    private final String ultimosDigitos;

    /**
     * Motivo de la eliminación.
     */
    private final String motivo;

    /**
     * Cantidad total de tarjetas que tiene el cliente después de eliminar esta.
     */
    private final int cantidadTarjetasRestantes;

    /**
     * Fecha y hora en que se eliminó la tarjeta.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de tarjeta eliminada.
     *
     * @param clienteId identificador del cliente
     * @param tarjetaId identificador de la tarjeta eliminada
     * @param tipoTarjeta tipo de tarjeta eliminada
     * @param ultimosDigitos últimos 4 dígitos de la tarjeta
     * @param motivo motivo de la eliminación
     * @param cantidadTarjetasRestantes cantidad de tarjetas restantes
     */
    public TarjetaEliminadaEvento(
            ClienteId clienteId,
            TarjetaId tarjetaId,
            TipoTarjeta tipoTarjeta,
            String ultimosDigitos,
            String motivo,
            int cantidadTarjetasRestantes
    ) {
        this.clienteId = clienteId;
        this.tarjetaId = tarjetaId;
        this.tipoTarjeta = tipoTarjeta;
        this.ultimosDigitos = ultimosDigitos;
        this.motivo = motivo;
        this.cantidadTarjetasRestantes = cantidadTarjetasRestantes;
        this.fechaEvento = LocalDateTime.now();
    }
}