package dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO de entrada para agregar una nueva tarjeta de crédito a un cliente.
 *
 * <p>Permite a un cliente agregar una tarjeta adicional a su cuenta,
 * respetando el límite máximo de 3 tarjetas por cliente.
 *
 * <p><strong>Validaciones:</strong>
 * <ul>
 *   <li>Número de tarjeta: 13-19 dígitos válidos</li>
 *   <li>Fecha de expiración: formato MM/YY y no expirada</li>
 *   <li>CVV: 3-4 dígitos según tipo de tarjeta</li>
 * </ul>
 *
 * <p><strong>Reglas de negocio:</strong>
 * <ul>
 *   <li>El cliente solo puede tener máximo 3 tarjetas</li>
 *   <li>Solo clientes ACTIVOS o EN_PROCESO_RESERVA pueden agregar tarjetas</li>
 *   <li>Clientes BLOQUEADOS o INACTIVOS no pueden agregar tarjetas</li>
 *   <li>La tarjeta debe ser validada antes de permitir su uso</li>
 * </ul>
 *
 * @param tarjeta datos de la nueva tarjeta a agregar
 *
 * @author javacadabra
 * @version 1.0.0
 */
public record AgregarTarjetaDTO(

        @NotNull(message = "Los datos de la tarjeta son obligatorios")
        @Valid
        CrearTarjetaDTO tarjeta

) implements Serializable {

    // Los métodos de utilidad están en CrearTarjetaDTO
}