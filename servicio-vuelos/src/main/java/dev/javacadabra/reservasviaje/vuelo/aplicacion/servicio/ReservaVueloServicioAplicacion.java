package dev.javacadabra.reservasviaje.vuelo.aplicacion.servicio;

import dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.entrada.CancelarVueloPuertoEntrada;
import dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.entrada.ReservarVueloPuertoEntrada;
import dev.javacadabra.reservasviaje.vuelo.aplicacion.puerto.salida.ReservaVueloRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.vuelo.dominio.excepcion.ReservaVueloNoEncontradaException;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.AsientoNumero;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.EstadoReservaVuelo;
import dev.javacadabra.reservasviaje.vuelo.dominio.modelo.objetovalor.ReservaVueloId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaVueloServicioAplicacion implements
        ReservarVueloPuertoEntrada,
        CancelarVueloPuertoEntrada {

    private final ReservaVueloRepositorioPuertoSalida repositorio;
    private final Random random = new Random();

    @Override
    @Transactional
    public ReservaVuelo reservarVuelo(
            String reservaViajeId,
            String clienteId,
            String origen,
            String destino) {

        log.info("✈️ Reservando vuelo: {} -> {} para reserva: {}",
                origen, destino, reservaViajeId);

        // Simular búsqueda de vuelo disponible
        String numeroVuelo = generarNumeroVuelo(origen, destino);
        String aerolinea = seleccionarAerolinea();
        AsientoNumero asiento = generarAsiento();

        // Crear la reserva
        ReservaVuelo reserva = ReservaVuelo.builder()
                .id(ReservaVueloId.generar())
                .reservaViajeId(reservaViajeId)
                .clienteId(clienteId)
                .origen(origen)
                .destino(destino)
                .fechaSalida(LocalDateTime.now().plusDays(7))
                .estado(EstadoReservaVuelo.PENDIENTE)
                .build();

        // Confirmar la reserva
        reserva.reservar(numeroVuelo, aerolinea, asiento);

        // Guardar
        reserva = repositorio.guardar(reserva);

        log.info("✅ Vuelo reservado: {} - Asiento: {} - Reserva: {}",
                numeroVuelo, asiento.valor(), reserva.getNumeroReserva());

        return reserva;
    }

    @Override
    @Transactional
    public void cancelarVuelo(String reservaViajeId) {
        log.info("❌ Cancelando vuelo para reserva: {}", reservaViajeId);

        ReservaVuelo reserva = repositorio.buscarPorReservaViajeId(reservaViajeId)
                .orElseThrow(() -> new ReservaVueloNoEncontradaException(reservaViajeId));

        reserva.cancelar();
        repositorio.guardar(reserva);

        log.info("✅ Vuelo cancelado: {}", reserva.getNumeroReserva());
    }

    // Métodos auxiliares para simulación
    private String generarNumeroVuelo(String origen, String destino) {
        String prefijo = (origen.substring(0, 1) + destino.substring(0, 1)).toUpperCase();
        return prefijo + (1000 + random.nextInt(9000));
    }

    private String seleccionarAerolinea() {
        String[] aerolineas = {"Iberia", "Vueling", "Air Europa", "Ryanair", "easyJet"};
        return aerolineas[random.nextInt(aerolineas.length)];
    }

    private AsientoNumero generarAsiento() {
        int fila = 1 + random.nextInt(30);
        char letra = (char) ('A' + random.nextInt(6)); // A-F
        return new AsientoNumero(fila + String.valueOf(letra));
    }
}
