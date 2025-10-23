package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO de salida resumido para un cliente.
 *
 * <p>Contiene información básica del cliente para uso en listados,
 * búsquedas o cuando no se necesita toda la información completa.
 *
 * <p>Este DTO es más ligero que {@link ClienteDTO} y se recomienda
 * para operaciones de consulta masiva o cuando el consumidor solo
 * necesita información básica del cliente.
 *
 * <p><strong>Casos de uso:</strong>
 * <ul>
 *   <li>Listados paginados de clientes</li>
 *   <li>Búsquedas por criterios</li>
 *   <li>Autocompletado en formularios</li>
 *   <li>Dashboards y reportes</li>
 * </ul>
 *
 * @param clienteId identificador único del cliente
 * @param nombreCompleto nombre completo del cliente
 * @param email email del cliente
 * @param estado estado actual del cliente
 * @param estadoDescripcion descripción legible del estado
 * @param ciudadResidencia ciudad de residencia
 * @param paisResidencia país de residencia
 * @param cantidadTarjetas cantidad de tarjetas registradas
 * @param puedeRealizarReservas indica si puede realizar reservas
 * @param fechaCreacion fecha de creación del cliente
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record ClienteResumenDTO(
        String clienteId,
        String nombreCompleto,
        String email,
        String estado,
        String estadoDescripcion,
        String ciudadResidencia,
        String paisResidencia,
        int cantidadTarjetas,
        boolean puedeRealizarReservas,
        LocalDateTime fechaCreacion
) implements Serializable {
}