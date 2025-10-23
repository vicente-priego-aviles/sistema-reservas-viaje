package dev.javacadabra.reservasviaje.cliente.dominio.evento;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evento de dominio publicado cuando un cliente actualiza sus datos personales.
 *
 * <p>Este evento se publica después de que los datos personales del cliente
 * han sido actualizados exitosamente (nombre, apellidos, email, teléfono).
 *
 * <p>No se incluyen en este evento los datos sensibles como el DNI completo,
 * solo se registran los campos que fueron modificados.
 *
 * <p>Consumidores potenciales de este evento:
 * <ul>
 *   <li>Servicio de notificaciones: Confirmar cambios al cliente</li>
 *   <li>Servicio de auditoría: Registrar modificación de datos personales</li>
 *   <li>Servicio de CRM: Sincronizar información actualizada</li>
 *   <li>Servicio de seguridad: Validar cambio de email si aplica</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@DomainEvent
@Getter
@ToString
@EqualsAndHashCode
public class DatosPersonalesActualizadosEvento {

    /**
     * Identificador único del cliente.
     */
    private final ClienteId clienteId;

    /**
     * Email del cliente (puede ser el nuevo si se actualizó).
     */
    private final String email;

    /**
     * Nombre completo del cliente actualizado.
     */
    private final String nombreCompleto;

    /**
     * Conjunto de campos que fueron modificados.
     * Ejemplos: "nombre", "apellidos", "email", "telefono"
     */
    private final Set<String> camposModificados;

    /**
     * Indica si el email fue modificado (requiere revalidación).
     */
    private final boolean emailModificado;

    /**
     * Fecha y hora en que se actualizaron los datos.
     */
    private final LocalDateTime fechaEvento;

    /**
     * Constructor del evento de datos personales actualizados.
     *
     * @param clienteId identificador del cliente
     * @param email email del cliente (nuevo si fue modificado)
     * @param nombreCompleto nombre completo actualizado
     * @param camposModificados conjunto de campos modificados
     * @param emailModificado si el email fue modificado
     */
    public DatosPersonalesActualizadosEvento(
            ClienteId clienteId,
            String email,
            String nombreCompleto,
            Set<String> camposModificados,
            boolean emailModificado
    ) {
        this.clienteId = clienteId;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.camposModificados = Set.copyOf(camposModificados); // Copia inmutable
        this.emailModificado = emailModificado;
        this.fechaEvento = LocalDateTime.now();
    }

    /**
     * Verifica si se modificó un campo específico.
     *
     * @param nombreCampo nombre del campo a verificar
     * @return true si el campo fue modificado, false en caso contrario
     */
    public boolean seModifico(String nombreCampo) {
        return camposModificados.contains(nombreCampo);
    }
}