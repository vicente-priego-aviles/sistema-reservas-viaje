package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada;

/**
 * Puerto de entrada para el caso de uso de cancelar reserva de coche.
 * Define el contrato que debe cumplir cualquier implementación.
 */
public interface CancelarCocheCasoUso {

    /**
     * Ejecuta el caso de uso de cancelar una reserva de coche.
     *
     * @param reservaId ID de la reserva a cancelar
     * @param motivo motivo de la cancelación
     */
    void ejecutar(String reservaId, String motivo);
}
