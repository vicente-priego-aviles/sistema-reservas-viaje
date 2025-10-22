package dev.javacadabra.reservasviaje.reserva.aplicacion.servicio;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarVueloDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaVueloRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.CancelarVueloCasoUso;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.ReservarVueloCasoUso;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.EventoPublicadorPuerto;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaVueloPuerto;
import dev.javacadabra.reservasviaje.reserva.dominio.evento.ReservaVueloCanceladaEvento;
import dev.javacadabra.reservasviaje.reserva.dominio.evento.ReservaVueloCreadaEvento;
import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.ReservaNoEncontradaException;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaVuelo;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.Pasajero;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que implementa los casos de uso de reserva de vuelo.
 * Orquesta las operaciones entre el dominio y la infraestructura.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaVueloServicio implements ReservarVueloCasoUso, CancelarVueloCasoUso {

    private final ReservaVueloPuerto reservaVueloPuerto;
    private final EventoPublicadorPuerto eventoPublicador;

    @Override
    public ReservaVueloRespuestaDTO ejecutar(ReservarVueloDTO dto) {
        log.info("🚀 Iniciando proceso de reserva de vuelo para cliente: {}", dto.clienteId());

        // 1. Crear Value Objects
        DatosVuelo datosVuelo = DatosVuelo.builder()
                .numeroVuelo(dto.numeroVuelo())
                .aerolinea(dto.aerolinea())
                .origen(dto.origen())
                .destino(dto.destino())
                .fechaSalida(dto.fechaSalida())
                .fechaLlegada(dto.fechaLlegada())
                .clase(dto.clase())
                .numeroPasajeros(dto.pasajeros().size())
                .build();

        // 2. Crear lista de pasajeros
        List<Pasajero> pasajeros = dto.pasajeros().stream()
                .map(p -> new Pasajero(
                        p.nombre(),
                        p.apellidos(),
                        p.numeroDocumento(),
                        p.tipoDocumento(),
                        p.fechaNacimiento(),
                        p.nacionalidad()
                ))
                .collect(Collectors.toList());

        // 3. Crear precio
        Currency moneda = dto.codigoMoneda() != null
                ? Currency.getInstance(dto.codigoMoneda())
                : Currency.getInstance("EUR");
        PrecioReserva precio = PrecioReserva.de(dto.precio(), moneda);

        // 4. Crear detalle de reserva
        DetalleReserva detalleReserva = new DetalleReserva(
                dto.clienteId(),
                dto.observaciones(),
                dto.codigoConfirmacion()
        );

        // 5. Crear agregado de dominio
        ReservaVuelo reservaVuelo = ReservaVuelo.crear(
                datosVuelo,
                pasajeros,
                precio,
                detalleReserva
        );

        log.info("✅ Reserva de vuelo creada con ID: {}", reservaVuelo.getReservaId().getValor());

        // 6. Persistir
        ReservaVuelo reservaGuardada = reservaVueloPuerto.guardar(reservaVuelo);

        log.info("💾 Reserva de vuelo persistida exitosamente");

        // 7. Publicar evento de dominio
        ReservaVueloCreadaEvento evento = new ReservaVueloCreadaEvento(
                reservaGuardada.getReservaId().getValor(),
                dto.clienteId(),
                dto.numeroVuelo(),
                dto.fechaSalida(),
                precio.getMonto()
        );
        eventoPublicador.publicar(evento);

        log.info("📨 Evento ReservaVueloCreadaEvento publicado");

        // 8. Mapear a DTO de respuesta
        return mapearARespuesta(reservaGuardada);
    }

    @Override
    public void ejecutar(String reservaId, String motivo) {
        log.info("🛑 Iniciando cancelación de reserva de vuelo: {}", reservaId);

        // 1. Buscar reserva
        ReservaVuelo reserva = reservaVueloPuerto.buscarPorId(ReservaId.de(reservaId))
                .orElseThrow(() -> ReservaNoEncontradaException.conId(ReservaId.de(reservaId)));

        // 2. Cancelar en el dominio (valida reglas de negocio)
        reserva.cancelar(motivo);

        log.info("✅ Reserva de vuelo cancelada: {}", reservaId);

        // 3. Persistir cambios
        reservaVueloPuerto.guardar(reserva);

        // 4. Publicar evento
        ReservaVueloCanceladaEvento evento = new ReservaVueloCanceladaEvento(
                reservaId,
                reserva.getDetalleReserva().getClienteId(),
                motivo
        );
        eventoPublicador.publicar(evento);

        log.info("📨 Evento ReservaVueloCanceladaEvento publicado");
    }

    /**
     * Mapea el agregado de dominio a DTO de respuesta.
     */
    private ReservaVueloRespuestaDTO mapearARespuesta(ReservaVuelo reserva) {
        List<ReservaVueloRespuestaDTO.PasajeroRespuestaDTO> pasajerosDTO =
                reserva.getPasajeros().stream()
                        .map(p -> new ReservaVueloRespuestaDTO.PasajeroRespuestaDTO(
                                p.getId(),
                                p.getNombre(),
                                p.getApellidos(),
                                p.getNumeroDocumento(),
                                p.getTipoDocumento()
                        ))
                        .collect(Collectors.toList());

        return new ReservaVueloRespuestaDTO(
                reserva.getReservaId().getValor(),
                reserva.getDatosVuelo().getNumeroVuelo(),
                reserva.getDatosVuelo().getAerolinea(),
                reserva.getDatosVuelo().getOrigen(),
                reserva.getDatosVuelo().getDestino(),
                reserva.getDatosVuelo().getFechaSalida(),
                reserva.getDatosVuelo().getFechaLlegada(),
                reserva.getDatosVuelo().getClase(),
                reserva.getPrecio().getMonto(),
                reserva.getPrecio().getCodigoMoneda(),
                reserva.getEstado().name(),
                pasajerosDTO,
                reserva.getDetalleReserva().getClienteId(),
                reserva.getDetalleReserva().getCodigoConfirmacion(),
                reserva.getFechaCreacion()
        );
    }
}
