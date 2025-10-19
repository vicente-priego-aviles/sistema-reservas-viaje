package dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.entrada;

import dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado.ReservaHotel;

/**
 * Puerto de entrada para reservar hoteles.
 *
 * <p>Define el contrato para el caso de uso de reserva de hotel.
 * Siguiendo arquitectura hexagonal, este puerto es implementado
 * por un servicio de aplicación y es invocado por adaptadores de entrada
 * (REST controllers, workers de Camunda, mensajería, etc.).</p>
 */
public interface ReservarHotelPuertoEntrada {

    /**
     * Reserva un hotel.
     *
     * @param reservaId ID de la reserva de viaje (correlación con proceso BPMN)
     * @param clienteId ID del cliente
     * @param destino Ciudad destino
     * @param fechaInicio Fecha de entrada (formato ISO: yyyy-MM-dd)
     * @param fechaFin Fecha de salida (formato ISO: yyyy-MM-dd)
     * @return La reserva de hotel creada
     */
    ReservaHotel reservarHotel(String reservaId, String clienteId, String destino,
                               String fechaInicio, String fechaFin);
}