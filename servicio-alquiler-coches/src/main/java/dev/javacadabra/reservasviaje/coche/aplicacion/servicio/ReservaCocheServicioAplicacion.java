package dev.javacadabra.reservasviaje.coche.aplicacion.servicio;

import dev.javacadabra.reservasviaje.coche.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.coche.aplicacion.puerto.salida.ReservaCocheRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.CategoriaCoche;

import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.EstadoReservaCoche;
import dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor.ReservaCocheId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaCocheServicioAplicacion implements
        ReservarCochePuertoEntrada,
        CancelarCochePuertoEntrada {

    private final ReservaCocheRepositorioPuertoSalida repositorio;
    private final Random random = new Random();

    @Override
    @Transactional
    public ReservaCoche reservarCoche(
            String reservaViajeId,
            String clienteId,
            String ciudad,
            String fechaRecogidaStr,
            String fechaDevolucionStr) {

        log.info("🚗 Reservando coche en {} para reserva: {}", ciudad, reservaViajeId);

        LocalDate fechaRecogida = LocalDate.parse(fechaRecogidaStr);
        LocalDate fechaDevolucion = LocalDate.parse(fechaDevolucionStr);

        // Simular búsqueda de coche disponible
        String modelo = generarModelo();
        String matricula = generarMatricula();
        CategoriaCoche categoria = seleccionarCategoria();
        String puntoRecogida = "Aeropuerto " + ciudad;

        // Crear la reserva
        ReservaCoche reserva = ReservaCoche.builder()
                .id(ReservaCocheId.generar())
                .reservaViajeId(reservaViajeId)
                .clienteId(clienteId)
                .ciudadRecogida(ciudad)
                .fechaRecogida(fechaRecogida)
                .fechaDevolucion(fechaDevolucion)
                .estado(EstadoReservaCoche.PENDIENTE)
                .build();

        // Confirmar la reserva
        reserva.reservar(modelo, matricula, categoria, puntoRecogida);

        // Guardar
        reserva = repositorio.guardar(reserva);

        log.info("✅ Coche reservado: {} - Matrícula: {} - Reserva: {}",
                modelo, matricula, reserva.getNumeroReserva());

        return reserva;
    }

    @Override
    @Transactional
    public void cancelarCoche(String reservaViajeId) {
        log.info("❌ Cancelando coche para reserva: {}", reservaViajeId);

        ReservaCoche reserva = repositorio.buscarPorReservaViajeId(reservaViajeId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.cancelar();
        repositorio.guardar(reserva);

        log.info("✅ Coche cancelado: {}", reserva.getNumeroReserva());
    }

    private String generarModelo() {
        String[] marcas = {"Toyota", "Volkswagen", "Ford", "Renault", "Seat"};
        String[] modelos = {"Corolla", "Golf", "Focus", "Megane", "Leon"};
        return marcas[random.nextInt(marcas.length)] + " " +
               modelos[random.nextInt(modelos.length)];
    }

    private String generarMatricula() {
        return String.format("%04d%s%s%s",
                random.nextInt(10000),
                (char)('A' + random.nextInt(26)),
                (char)('A' + random.nextInt(26)),
                (char)('A' + random.nextInt(26)));
    }

    private CategoriaCoche seleccionarCategoria() {
        CategoriaCoche[] categorias = CategoriaCoche.values();
        return categorias[random.nextInt(categorias.length)];
    }
}