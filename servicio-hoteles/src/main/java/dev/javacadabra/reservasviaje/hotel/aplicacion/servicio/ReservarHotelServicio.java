package dev.javacadabra.reservasviaje.hotel.aplicacion.servicio;

import dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.entrada.ReservarHotelPuertoEntrada;
import dev.javacadabra.reservasviaje.hotel.aplicacion.puerto.salida.ReservaHotelRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.HabitacionNumero;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.ReservaHotelId;
import dev.javacadabra.reservasviaje.hotel.dominio.modelo.objetovalor.TipoHabitacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Servicio de aplicación que implementa el caso de uso de reservar hotel.
 *
 * <p>Orquesta la lógica de negocio y coordina entre el dominio y
 * los adaptadores de salida (repositorio).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservarHotelServicio implements ReservarHotelPuertoEntrada {

    private final ReservaHotelRepositorioPuertoSalida repositorio;
    private final Random random = new Random();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static LocalDate parseFecha(String s) {
        int tIndex = s.indexOf('T');
        String datePart = tIndex > 0 ? s.substring(0, tIndex) : s;
        return LocalDate.parse(datePart, FORMATTER);
    }

    @Override
    @Transactional
    public ReservaHotel reservarHotel(String reservaId, String clienteId, String destino,
                                      String fechaInicio, String fechaFin) {

        log.info("📝 Iniciando reserva de hotel para reservaViajeId: {}", reservaId);

        // Validar que no exista ya una reserva para este viaje
        if (repositorio.existePorReservaViajeId(reservaId)) {
            throw new IllegalStateException(
                    "Ya existe una reserva de hotel para el viaje: " + reservaId
            );
        }

        // Parsear fechas (admite ISO datetime "2024-01-16T10:00:00" y date "2024-01-16")
        LocalDate fechaEntrada = parseFecha(fechaInicio);
        LocalDate fechaSalida = parseFecha(fechaFin);

        // Crear el agregado de dominio
        ReservaHotel reserva = ReservaHotel.builder()
                .id(ReservaHotelId.generar())
                .reservaViajeId(reservaId)
                .clienteId(clienteId)
                .ciudad(destino)
                .fechaEntrada(fechaEntrada)
                .fechaSalida(fechaSalida)
                .build();

        // Simular datos del hotel
        String nombreHotel = generarNombreHotel(destino);
        HabitacionNumero numeroHabitacion = generarNumeroHabitacion();
        TipoHabitacion tipoHabitacion = seleccionarTipoHabitacion();

        // Invocar método del dominio para reservar
        reserva.reservar(nombreHotel, numeroHabitacion, tipoHabitacion);

        // Persistir usando el puerto de salida
        ReservaHotel reservaGuardada = repositorio.guardar(reserva);

        log.info("✅ Hotel reservado: {} - Habitación: {}",
                nombreHotel, numeroHabitacion.valor());

        return reservaGuardada;
    }

    /**
     * Genera un nombre de hotel simulado basado en el destino.
     */
    private String generarNombreHotel(String ciudad) {
        String[] prefijos = {"Hotel", "Grand Hotel", "Hostal", "Resort", "Inn"};
        String[] sufijos = {"Central", "Plaza", "Royal", "Palace", "Suites"};

        String prefijo = prefijos[random.nextInt(prefijos.length)];
        String sufijo = sufijos[random.nextInt(sufijos.length)];

        return String.format("%s %s %s", prefijo, ciudad, sufijo);
    }

    /**
     * Genera un número de habitación aleatorio.
     */
    private HabitacionNumero generarNumeroHabitacion() {
        int piso = random.nextInt(10) + 1; // Pisos 1-10
        int numero = random.nextInt(20) + 1; // Números 1-20
        return new HabitacionNumero(String.format("%d%02d", piso, numero));
    }

    /**
     * Selecciona un tipo de habitación aleatorio.
     */
    private TipoHabitacion seleccionarTipoHabitacion() {
        TipoHabitacion[] tipos = TipoHabitacion.values();
        return tipos[random.nextInt(tipos.length)];
    }
}
