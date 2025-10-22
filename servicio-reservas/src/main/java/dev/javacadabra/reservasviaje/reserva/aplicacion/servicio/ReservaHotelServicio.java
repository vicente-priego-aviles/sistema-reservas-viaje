package dev.javacadabra.reservasviaje.reserva.aplicacion.servicio;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarHotelDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaHotelRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.CancelarHotelCasoUso;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.ReservarHotelCasoUso;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.EventoPublicadorPuerto;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaHotelPuerto;
import dev.javacadabra.reservasviaje.reserva.dominio.evento.ReservaHotelCanceladaEvento;
import dev.javacadabra.reservasviaje.reserva.dominio.evento.ReservaHotelCreadaEvento;
import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.ReservaNoEncontradaException;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaHotel;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.DatosHotel;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.PrecioReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

/**
 * Servicio de aplicación que implementa los casos de uso de reserva de hotel.
 * Orquesta las operaciones entre el dominio y la infraestructura.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaHotelServicio implements ReservarHotelCasoUso, CancelarHotelCasoUso {

    private final ReservaHotelPuerto reservaHotelPuerto;
    private final EventoPublicadorPuerto eventoPublicador;

    @Override
    public ReservaHotelRespuestaDTO ejecutar(ReservarHotelDTO dto) {
        log.info("🚀 Iniciando proceso de reserva de hotel para cliente: {}", dto.clienteId());

        // 1. Crear Value Objects
        DatosHotel datosHotel = DatosHotel.builder()
                .nombreHotel(dto.nombreHotel())
                .ciudad(dto.ciudad())
                .direccion(dto.direccion())
                .fechaEntrada(dto.fechaEntrada())
                .fechaSalida(dto.fechaSalida())
                .tipoHabitacion(dto.tipoHabitacion())
                .numeroHabitaciones(dto.numeroHabitaciones())
                .numeroHuespedes(dto.numeroHuespedes())
                .build();

        // 2. Crear precio
        Currency moneda = dto.codigoMoneda() != null
                ? Currency.getInstance(dto.codigoMoneda())
                : Currency.getInstance("EUR");
        PrecioReserva precio = PrecioReserva.de(dto.precio(), moneda);

        // 3. Crear detalle de reserva
        DetalleReserva detalleReserva = new DetalleReserva(
                dto.clienteId(),
                dto.observaciones(),
                dto.codigoConfirmacion()
        );

        // 4. Crear agregado de dominio
        ReservaHotel reservaHotel = ReservaHotel.crear(
                datosHotel,
                precio,
                detalleReserva
        );

        log.info("✅ Reserva de hotel creada con ID: {}", reservaHotel.getReservaId().getValor());

        // 5. Persistir
        ReservaHotel reservaGuardada = reservaHotelPuerto.guardar(reservaHotel);

        log.info("💾 Reserva de hotel persistida exitosamente");

        // 6. Publicar evento de dominio
        ReservaHotelCreadaEvento evento = new ReservaHotelCreadaEvento(
                reservaGuardada.getReservaId().getValor(),
                dto.clienteId(),
                dto.nombreHotel(),
                dto.fechaEntrada(),
                dto.fechaSalida(),
                precio.getMonto()
        );
        eventoPublicador.publicar(evento);

        log.info("📨 Evento ReservaHotelCreadaEvento publicado");

        // 7. Mapear a DTO de respuesta
        return mapearARespuesta(reservaGuardada);
    }

    @Override
    public void ejecutar(String reservaId, String motivo) {
        log.info("🛑 Iniciando cancelación de reserva de hotel: {}", reservaId);

        // 1. Buscar reserva
        ReservaHotel reserva = reservaHotelPuerto.buscarPorId(ReservaId.de(reservaId))
                .orElseThrow(() -> ReservaNoEncontradaException.conId(ReservaId.de(reservaId)));

        // 2. Cancelar en el dominio (valida reglas de negocio)
        reserva.cancelar(motivo);

        log.info("✅ Reserva de hotel cancelada: {}", reservaId);

        // 3. Persistir cambios
        reservaHotelPuerto.guardar(reserva);

        // 4. Publicar evento
        ReservaHotelCanceladaEvento evento = new ReservaHotelCanceladaEvento(
                reservaId,
                reserva.getDetalleReserva().getClienteId(),
                motivo
        );
        eventoPublicador.publicar(evento);

        log.info("📨 Evento ReservaHotelCanceladaEvento publicado");
    }

    /**
     * Mapea el agregado de dominio a DTO de respuesta.
     */
    private ReservaHotelRespuestaDTO mapearARespuesta(ReservaHotel reserva) {
        return new ReservaHotelRespuestaDTO(
                reserva.getReservaId().getValor(),
                reserva.getDatosHotel().getNombreHotel(),
                reserva.getDatosHotel().getCiudad(),
                reserva.getDatosHotel().getDireccion(),
                reserva.getDatosHotel().getFechaEntrada(),
                reserva.getDatosHotel().getFechaSalida(),
                reserva.getDatosHotel().getTipoHabitacion(),
                reserva.getDatosHotel().getNumeroHabitaciones(),
                reserva.getDatosHotel().getNumeroHuespedes(),
                reserva.calcularNoches(),
                reserva.getPrecio().getMonto(),
                reserva.getPrecio().getCodigoMoneda(),
                reserva.getEstado().name(),
                reserva.getDetalleReserva().getClienteId(),
                reserva.getDetalleReserva().getCodigoConfirmacion(),
                reserva.getFechaCreacion()
        );
    }
}
