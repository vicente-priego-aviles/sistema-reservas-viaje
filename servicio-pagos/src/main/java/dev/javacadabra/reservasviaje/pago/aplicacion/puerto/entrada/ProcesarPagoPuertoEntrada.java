package dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;

/**
 * Puerto de entrada para procesar pagos.
 *
 * <p>Define el contrato para el caso de uso de procesamiento de pago.
 * Siguiendo arquitectura hexagonal, este puerto es implementado
 * por un servicio de aplicación.</p>
 */
public interface ProcesarPagoPuertoEntrada {

    /**
     * Procesa un pago para una reserva de viaje.
     *
     * @param reservaId ID de la reserva de viaje
     * @param clienteId ID del cliente
     * @param monto Monto a pagar
     * @return El pago procesado
     * @throws dev.javacadabra.reservasviaje.pago.dominio.excepcion.MontoExcedeLimiteException si el monto excede el límite
     */
    Pago procesarPago(String reservaId, String clienteId, Double monto);
}
