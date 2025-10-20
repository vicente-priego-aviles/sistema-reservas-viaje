package dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada;

/**
 * Puerto de entrada para revertir pagos.
 *
 * <p>Define el contrato para el caso de uso de compensación/reversión
 * de pagos cuando ocurre un error en el proceso.</p>
 */
public interface RevertirPagoPuertoEntrada {

    /**
     * Revierte un pago previamente procesado.
     *
     * @param reservaId ID de la reserva de viaje
     * @param motivo Motivo de la reversión
     */
    void revertirPago(String reservaId, String motivo);
}