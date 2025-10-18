package dev.javacadabra.reservasviaje.coche.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado.ReservaCoche;

/**
 * Puerto de entrada para el caso de uso de reservar un coche.
 *
 * <p>Define el contrato para realizar una reserva de coche de alquiler
 * como parte del proceso de reserva de viaje. Este puerto será invocado
 * por los adaptadores de entrada (workers de Camunda, REST controllers, etc.)</p>
 *
 * <p><strong>Patrón:</strong> Arquitectura Hexagonal - Puerto de Entrada (Use Case)</p>
 *
 * @author JavaCadabra
 */
public interface ReservarCochePuertoEntrada {

    /**
     * Reserva un coche de alquiler para una reserva de viaje.
     *
     * <p>Este método realiza las siguientes operaciones:
     * <ul>
     *   <li>Valida los datos de entrada</li>
     *   <li>Busca disponibilidad de vehículos</li>
     *   <li>Crea el agregado ReservaCoche</li>
     *   <li>Confirma la reserva con los detalles del vehículo</li>
     *   <li>Persiste la reserva</li>
     * </ul>
     * </p>
     *
     * @param reservaViajeId identificador de la reserva de viaje principal
     * @param clienteId identificador del cliente que realiza la reserva
     * @param ciudad ciudad donde se recogerá el vehículo
     * @param fechaRecogidaStr fecha de recogida en formato ISO (YYYY-MM-DD)
     * @param fechaDevolucionStr fecha de devolución en formato ISO (YYYY-MM-DD)
     * @return el agregado ReservaCoche con todos los detalles de la reserva
     * @throws IllegalArgumentException si los parámetros no son válidos
     * @throws RuntimeException si no hay vehículos disponibles
     */
    ReservaCoche reservarCoche(
            String reservaViajeId,
            String clienteId,
            String ciudad,
            String fechaRecogidaStr,
            String fechaDevolucionStr);
}
