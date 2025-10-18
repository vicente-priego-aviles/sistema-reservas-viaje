package dev.javacadabra.reservasviaje.coche.aplicacion.puerto.entrada;

/**
 * Puerto de entrada para el caso de uso de cancelar una reserva de coche.
 *
 * <p>Define el contrato para cancelar una reserva de coche existente.
 * Este puerto se utiliza típicamente en procesos de compensación (Saga Pattern)
 * cuando alguna parte del flujo de reserva falla y se deben revertir las operaciones.</p>
 *
 * <p><strong>Patrón:</strong> Arquitectura Hexagonal - Puerto de Entrada (Use Case)</p>
 * <p><strong>Patrón BPMN:</strong> Compensation Handler - Invocado desde eventos de compensación</p>
 *
 * @author JavaCadabra
 */
public interface CancelarCochePuertoEntrada {

    /**
     * Cancela una reserva de coche existente.
     *
     * <p>Este método realiza las siguientes operaciones:
     * <ul>
     *   <li>Busca la reserva por el ID de la reserva de viaje</li>
     *   <li>Valida que la reserva esté en estado RESERVADA</li>
     *   <li>Cambia el estado a CANCELADA</li>
     *   <li>Registra la fecha de cancelación</li>
     *   <li>Persiste los cambios</li>
     * </ul>
     * </p>
     *
     * <p><strong>Nota:</strong> Este método es idempotente. Si se invoca varias veces
     * para la misma reserva, no generará errores.</p>
     *
     * @param reservaViajeId identificador de la reserva de viaje principal
     * @throws RuntimeException si la reserva no existe
     * @throws IllegalStateException si la reserva no puede ser cancelada (estado inválido)
     */
    void cancelarCoche(String reservaViajeId);
}
