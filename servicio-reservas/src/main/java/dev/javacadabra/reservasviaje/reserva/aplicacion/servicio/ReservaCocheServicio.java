package dev.javacadabra.reservasviaje.reserva.aplicacion.servicio;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.ReservarCocheDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaCocheRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.CancelarCocheCasoUso;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.ReservarCocheCasoUso;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.EventoPublicadorPuerto;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaCochePuerto;
import dev.javacadabra.reservasviaje.reserva.dominio.evento.ReservaCocheCanceladaEvento;
import dev.javacadabra.reservasviaje.reserva.dominio.evento.ReservaCocheCreadaEvento;
import dev.javacadabra.reservasviaje.reserva.dominio.excepcion.ReservaNoEncontradaException;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaCoche;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad.DetalleReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.DatosCoche;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.PrecioReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

/**
 * Servicio de aplicación que implementa los casos de uso de reserva de coche.
 * Orquesta las operaciones entre el dominio y la infraestructura.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaCocheServicio implements ReservarCocheCasoUso, CancelarCocheCasoUso {

    private final ReservaCochePuerto reservaCochePuerto;
    private final EventoPublicadorPuerto eventoPublicador;

    @Override
    public ReservaCocheRespuestaDTO ejecutar(ReservarCocheDTO dto) {
        log.info("🚀 Iniciando proceso de reserva de coche para cliente: {}", dto.clienteId());

        // 1. Crear Value Objects
        DatosCoche datosCoche = DatosCoche.builder()
                .empresaAlquiler(dto.empresaAlquiler())
                .modeloCoche(dto.modeloCoche())
                .categoriaCoche(dto.categoriaCoche())
                .ubicacionRecogida(dto.ubicacionRecogida())
                .ubicacionDevolucion(dto.ubicacionDevolucion())
                .fechaRecogida(dto.fechaRecogida())
                .fechaDevolucion(dto.fechaDevolucion())
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
        ReservaCoche reservaCoche = ReservaCoche.crear(
                datosCoche,
                precio,
                detalleReserva
        );

        log.info("✅ Reserva de coche creada con ID: {}", reservaCoche.getReservaId().getValor());

        // 5. Persistir
        ReservaCoche reservaGuardada = reservaCochePuerto.guardar(reservaCoche);

        log.info("💾 Reserva de coche persistida exitosamente");

        // 6. Publicar evento de dominio
        ReservaCocheCreadaEvento evento = new ReservaCocheCreadaEvento(
                reservaGuardada.getReservaId().getValor(),
                dto.clienteId(),
                dto.modeloCoche(),
                dto.fechaRecogida(),
                dto.fechaDevolucion(),
                precio.getMonto()
        );
        eventoPublicador.publicar(evento);

        log.info("📨 Evento ReservaCocheCreadaEvento publicado");

        // 7. Mapear a DTO de respuesta
        return mapearARespuesta(reservaGuardada);
    }

    @Override
    public void ejecutar(String reservaId, String motivo) {
        log.info("🛑 Iniciando cancelación de reserva de coche: {}", reservaId);

        // 1. Buscar reserva
        ReservaCoche reserva = reservaCochePuerto.buscarPorId(ReservaId.de(reservaId))
                .orElseThrow(() -> ReservaNoEncontradaException.conId(ReservaId.de(reservaId)));

        // 2. Cancelar en el dominio (valida reglas de negocio)
        reserva.cancelar(motivo);

        log.info("✅ Reserva de coche cancelada: {}", reservaId);

        // 3. Persistir cambios
        reservaCochePuerto.guardar(reserva);

        // 4. Publicar evento
        ReservaCocheCanceladaEvento evento = new ReservaCocheCanceladaEvento(
                reservaId,
                reserva.getDetalleReserva().getClienteId(),
                motivo
        );
        eventoPublicador.publicar(evento);

        log.info("📨 Evento ReservaCocheCanceladaEvento publicado");
    }

    /**
     * Mapea el agregado de dominio a DTO de respuesta.
     */
    private ReservaCocheRespuestaDTO mapearARespuesta(ReservaCoche reserva) {
        return new ReservaCocheRespuestaDTO(
                reserva.getReservaId().getValor(),
                reserva.getDatosCoche().getEmpresaAlquiler(),
                reserva.getDatosCoche().getModeloCoche(),
                reserva.getDatosCoche().getCategoriaCoche(),
                reserva.getDatosCoche().getUbicacionRecogida(),
                reserva.getDatosCoche().getUbicacionDevolucion(),
                reserva.getDatosCoche().getFechaRecogida(),
                reserva.getDatosCoche().getFechaDevolucion(),
                reserva.calcularDiasAlquiler(),
                reserva.getPrecio().getMonto(),
                reserva.getPrecio().getCodigoMoneda(),
                reserva.getEstado().name(),
                reserva.getDetalleReserva().getClienteId(),
                reserva.getDetalleReserva().getCodigoConfirmacion(),
                reserva.getFechaCreacion()
        );
    }
}
