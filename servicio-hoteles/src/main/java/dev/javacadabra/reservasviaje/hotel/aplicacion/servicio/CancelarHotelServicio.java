package dev.javacadabra.reservasviaje.hotel.aplicacion.servicio;

import dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.entrada.CancelarHotelPuertoEntrada;
import dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.salida.ReservaHotelRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado.ReservaHotel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que implementa el caso de uso de cancelar hotel.
 *
 * <p>Orquesta la lógica de negocio para cancelar una reserva existente.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelarHotelServicio implements CancelarHotelPuertoEntrada {

    private final ReservaHotelRepositorioPuertoSalida repositorio;

    @Override
    @Transactional
    public void cancelarHotel(String reservaId) {
        log.info("🔄 Iniciando cancelación de hotel para reservaViajeId: {}", reservaId);

        // Buscar la reserva por reservaViajeId
        ReservaHotel reserva = repositorio.buscarPorReservaViajeId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe reserva de hotel para el viaje: " + reservaId
                ));

        // Invocar método del dominio para cancelar
        reserva.cancelar();

        // Persistir el cambio de estado
        repositorio.guardar(reserva);

        log.info("✅ Reserva de hotel cancelada para reservaViajeId: {}", reservaId);
    }
}
