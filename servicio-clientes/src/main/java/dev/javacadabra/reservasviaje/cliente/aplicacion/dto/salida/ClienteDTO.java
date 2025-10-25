package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida completo para un cliente.
 *
 * <p>Contiene toda la información de un cliente incluyendo:
 * <ul>
 *   <li>Identificador único</li>
 *   <li>Datos personales (con DNI enmascarado)</li>
 *   <li>Dirección postal completa</li>
 *   <li>Estado actual del cliente</li>
 *   <li>Tarjetas de crédito (con números enmascarados)</li>
 *   <li>Información de auditoría (fechas de creación y actualización)</li>
 * </ul>
 *
 * <p>Este DTO se usa en respuestas de consulta individual de clientes
 * donde se necesita toda la información disponible.
 *
 * @param clienteId identificador único del cliente
 * @param datosPersonales datos personales del cliente
 * @param direccion dirección postal del cliente
 * @param estado estado actual del cliente (ACTIVO, PENDIENTE_VALIDACION, etc.)
 * @param estadoDescripcion descripción legible del estado
 * @param tarjetas lista de tarjetas de crédito del cliente
 * @param cantidadTarjetas cantidad total de tarjetas
 * @param tieneTarjetasValidas indica si tiene al menos una tarjeta válida
 * @param puedeRealizarPagos indica si puede realizar Pagos
 * @param estaActivo indica si el cliente está en estado ACTIVO
 * @param estaBloqueado indica si el cliente está bloqueado
 * @param estaEnProcesoReserva indica si tiene una reserva en proceso
 * @param motivoBloqueo motivo del bloqueo (si aplica, puede ser null)
 * @param fechaCreacion fecha y hora de creación del cliente
 * @param fechaActualizacion fecha y hora de última actualización
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record ClienteDTO(
        String clienteId,
        DatosPersonalesDTO datosPersonales,
        DireccionDTO direccion,
        String estado,
        String estadoDescripcion,
        List<TarjetaCreditoDTO> tarjetas,
        int cantidadTarjetas,
        boolean tieneTarjetasValidas,
        boolean puedeRealizarPagos,
        boolean estaActivo,
        boolean estaBloqueado,
        boolean estaEnProcesoReserva,
        String motivoBloqueo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) implements Serializable {
}