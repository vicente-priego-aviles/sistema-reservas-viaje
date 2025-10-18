package dev.javacadabra.reservasviaje.hotel.aplicacion.servicio;

import dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.salida.ReservaHotelRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.EstadoReservaHotel;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.HabitacionNumero;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.ReservaHotelId;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.TipoHabitacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaHotelServicioAplicacion implements
        ReservarHotelPuertoEntrada,
        CancelarHotelPuertoEntrada {

    private final ReservaHotelRepositorioPuertoSalida repositorio;
    private final Random random = new Random();

    @Override
    @Transactional
    public ReservaHotel reservarHotel(
            String reservaViajeId,
            String clienteId,
            String ciudad,
            String fechaEntradaStr,
            String fechaSalidaStr) {

        log.info("🏨 Reservando hotel en {} para reserva: {}", ciudad, reservaViajeId);

        LocalDate fechaEntrada = LocalDate.parse(fechaEntradaStr);
        LocalDate fechaSalida = LocalDate.parse(fechaSalidaStr);

        // Simular búsqueda de hotel disponible
        String nombreHotel = generarNombreHotel(ciudad);
        HabitacionNumero numeroHabitacion = generarHabitacion();
        TipoHabitacion tipoHabitacion = seleccionarTipoHabitacion();

        // Crear la reserva
        ReservaHotel reserva = ReservaHotel.builder()
                .id(ReservaHotelId.generar())
                .reservaViajeId(reservaViajeId)
                .clienteId(clienteId)
                .ciudad(ciudad)
                .fechaEntrada(fechaEntrada)
                .fechaSalida(fechaSalida)
                .estado(EstadoReservaHotel.PENDIENTE)
                .build();

        // Confirmar la reserva
        reserva.reservar(nombreHotel, numeroHabitacion, tipoHabitacion);

        // Guardar
        reserva = repositorio.guardar(reserva);

        log.info("✅ Hotel reservado: {} - Habitación: {} - Reserva: {}",
                nombreHotel, numeroHabitacion.valor(), reserva.getNumeroReserva());

        return reserva;
    }

    @Override
    @Transactional
    public void cancelarHotel(String reservaViajeId) {
        log.info("❌ Cancelando hotel para reserva: {}", reservaViajeId);

        ReservaHotel reserva = repositorio.buscarPorReservaViajeId(reservaViajeId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.cancelar();
        repositorio.guardar(reserva);

        log.info("✅ Hotel cancelado: {}", reserva.getNumeroReserva());
    }

    private String generarNombreHotel(String ciudad) {
        String[] prefijos = {"Hotel", "Hostal", "Resort", "Aparthotel"};
        String[] sufijos = {"Central", "Plaza", "Boutique", "Suites", "Residence"};
        return prefijos[random.nextInt(prefijos.length)] + " " +
               ciudad + " " +
               sufijos[random.nextInt(sufijos.length)];
    }

    private HabitacionNumero generarHabitacion() {
        int numero = 100 + random.nextInt(900);
        return new HabitacionNumero(String.valueOf(numero));
    }

    private TipoHabitacion seleccionarTipoHabitacion() {
        TipoHabitacion[] tipos = TipoHabitacion.values();
        return tipos[random.nextInt(tipos.length)];
    }
}
