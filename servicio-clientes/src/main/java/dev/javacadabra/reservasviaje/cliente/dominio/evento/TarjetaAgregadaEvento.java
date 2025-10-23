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
 * Evento de dominio publicado cuando se agrega una nueva tarjeta de crédito a un cliente.
 *
 * <p>Este evento se publica después de que una tarjeta ha sido agregada
 * exitosamente al cliente, pasando todas las validaciones de negocio
 * (límite de tarjetas, formato válido, etc.).
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Confirmar al cliente la nueva tarjeta</li>
 *   <li>Servicio de seguridad: Validar tarjeta con proveedor de pagos</li>
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
public class TarjetaAgregadaEvento {

    /**
     * Identificador único del cliente.
     */
    private final ClienteId clienteId;

    /**
     * Identificador único de la tarjeta agregada.
     */
    private final TarjetaId tarjetaId;

    /**
     * Tipo de tarjeta (Visa, Mastercard, Amex, etc.).
     */
    private final TipoTarjeta tipoTarjeta;

    /**
     * Últimos 4 dígitos de la tarjeta (para identificación segura).
     */
    private final String ultimosDigitos;

    /**
     * Cantidad total de tarjetas que tiene el cliente después de agregar esta.
     */
    private final int cantidadTarjetasTotal;

    /**
     * Fecha y hora en que se agregó la tarjeta.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de tarjeta agregada.
     *
     * @param clienteId identificador del cliente
     * @param tarjetaId identificador de la tarjeta
     * @param tipoTarjeta tipo de tarjeta
     * @param ultimosDigitos últimos 4 dígitos de la tarjeta
     * @param cantidadTarjetasTotal cantidad total de tarjetas del cliente
     */
    public TarjetaAgregadaEvento(
            ClienteId clienteId,
            TarjetaId tarjetaId,
            TipoTarjeta tipoTarjeta,
            String ultimosDigitos,
            int cantidadTarjetasTotal
    ) {
        this.clienteId = clienteId;
        this.tarjetaId = tarjetaId;
        this.tipoTarjeta = tipoTarjeta;
        this.ultimosDigitos = ultimosDigitos;
        this.cantidadTarjetasTotal = cantidadTarjetasTotal;
        this.fechaEvento = LocalDateTime.now();
    }

    /**
     * Verifica si el cliente ha alcanzado el límite máximo de tarjetas.
     *
     * @return true si tiene 3 tarjetas (límite máximo), false en caso contrario
     */
    public boolean alcanzadoLimiteMaximo() {
        return cantidadTarjetasTotal >= 3;
    }
}