package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.EstadoCliente;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando se crea un nuevo cliente en el sistema.
 *
 * <p>Este evento se publica después de que el agregado Cliente ha sido
 * creado exitosamente con todas sus validaciones de negocio.
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Enviar email de bienvenida</li>
 *   <li>Servicio de autenticación: Crear credenciales de acceso</li>
 *   <li>Servicio de auditoría: Registrar creación de cliente</li>
 *   <li>Servicio de CRM: Actualizar base de datos de marketing</li>
 *   <li>Proceso Camunda: Iniciar flujo de validación de cliente</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@DomainEvent
@Getter
@ToString
@EqualsAndHashCode
public class ClienteCreadoEvento {

    /**
     * Identificador único del cliente creado.
     */
    private final ClienteId clienteId;

    /**
     * Email del cliente (usado como identificador de usuario).
     */
    private final String email;

    /**
     * Nombre completo del cliente.
     */
    private final String nombreCompleto;

    /**
     * DNI del cliente (enmascarado por seguridad).
     */
    private final String dniEnmascarado;

    /**
     * Estado inicial del cliente (normalmente PENDIENTE_VALIDACION).
     */
    private final EstadoCliente estado;

    /**
     * Cantidad de tarjetas registradas al momento de creación.
     */
    private final int cantidadTarjetas;

    /**
     * Fecha y hora en que ocurrió el evento.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de cliente creado.
     *
     * @param clienteId identificador del cliente
     * @param email email del cliente
     * @param nombreCompleto nombre completo del cliente
     * @param dniEnmascarado DNI enmascarado del cliente
     * @param estado estado inicial del cliente
     * @param cantidadTarjetas cantidad de tarjetas registradas
     */
    public ClienteCreadoEvento(
            ClienteId clienteId,
            String email,
            String nombreCompleto,
            String dniEnmascarado,
            EstadoCliente estado,
            int cantidadTarjetas
    ) {
        this.clienteId = clienteId;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.dniEnmascarado = dniEnmascarado;
        this.estado = estado;
        this.cantidadTarjetas = cantidadTarjetas;
        this.fechaEvento = LocalDateTime.now();
    }

    /**
     * Verifica si el cliente requiere validación.
     *
     * @return true si el estado es PENDIENTE_VALIDACION, false en caso contrario
     */
    public boolean requiereValidacion() {
        return estado == EstadoCliente.PENDIENTE_VALIDACION;
    }
}
