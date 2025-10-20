package dev.javacadabra.reservasviaje.pago.aplicacion.servicio;


import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.entrada.ProcesarPagoPuertoEntrada;
import dev.javacadabra.reservasviaje.pago.aplicacion.puerto.salida.PagoRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado.Pago;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que implementa el caso de uso de procesar pago.
 *
 * <p>Orquesta la lógica de negocio y coordina entre el dominio y
 * los adaptadores de salida (repositorio).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarPagoServicio implements ProcesarPagoPuertoEntrada {

    private final PagoRepositorioPuertoSalida repositorio;

    @Override
    @Transactional
    public Pago procesarPago(String reservaId, String clienteId, Double monto) {
        log.info("💳 Iniciando procesamiento de pago para reservaViajeId: {} - Monto: {}€",
                reservaId, monto);

        // Validar que no exista ya un pago para este viaje
        if (repositorio.existePorReservaViajeId(reservaId)) {
            log.warn("⚠️ Ya existe un pago para la reserva: {}", reservaId);
            // Recuperar el pago existente en lugar de lanzar excepción
            return repositorio.buscarPorReservaViajeId(reservaId)
                    .orElseThrow(() -> new IllegalStateException("Error al recuperar pago existente"));
        }

        // Crear el agregado de dominio
        Pago pago = Pago.builder()
                .id(PagoId.generar())
                .reservaViajeId(reservaId)
                .clienteId(clienteId)
                .monto(new Monto(monto))
                .metodoPago(seleccionarMetodoPago()) // Simulación
                .estado(EstadoPago.PROCESANDO)
                .build();

        try {
            // Invocar método del dominio para procesar
            pago.procesar();

            // Persistir usando el puerto de salida
            Pago pagoGuardado = repositorio.guardar(pago);

            log.info("✅ Pago procesado exitosamente - Transacción: {}",
                    pagoGuardado.getNumeroTransaccion());

            return pagoGuardado;

        } catch (Exception e) {
            log.error("❌ Error al procesar pago: {}", e.getMessage());

            // Marcar el pago como fallido
            pago.fallar(e.getMessage());
            repositorio.guardar(pago);

            throw e;
        }
    }

    /**
     * Selecciona un método de pago aleatorio (simulación).
     */
    private MetodoPago seleccionarMetodoPago() {
        // En un caso real, esto vendría de la petición
        return MetodoPago.TARJETA_CREDITO;
    }
}
