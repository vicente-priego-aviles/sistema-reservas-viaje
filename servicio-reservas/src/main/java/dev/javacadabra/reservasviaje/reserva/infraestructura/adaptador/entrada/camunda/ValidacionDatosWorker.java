package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaViaje;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.EstadoReserva;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.ReservaId;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker que valida los datos de entrada de la reserva.
 * <p>
 * Validaciones:
 * - Fechas futuras
 * - Monto positivo
 * - Origen y destino diferentes
 * - Fecha fin posterior a fecha inicio
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidacionDatosWorker {

    private final ReservaRepositorioPuertoSalida repositorio;

    @JobWorker(type = "validar-datos", autoComplete = true)
    public Map<String, Object> validarDatos(
            final ActivatedJob job,
            @Variable String reservaId,
            @Variable String origen,
            @Variable String destino,
            @Variable String fechaInicio,
            @Variable String fechaFin,
            @Variable Double monto) {

        log.info("🔍 Validando datos de reserva: {}", reservaId);

        Map<String, Object> variables = new HashMap<>();

        try {
            // 1. Validar origen y destino diferentes
            if (origen.equalsIgnoreCase(destino)) {
                log.warn("⚠️ Origen y destino son iguales: {}", origen);
                variables.put("datosValidos", false);
                variables.put("motivoRechazo", "Origen y destino deben ser diferentes");
                return variables;
            }

            // 2. Validar fechas
            LocalDate fechaInicioDate = LocalDate.parse(fechaInicio);
            LocalDate fechaFinDate = LocalDate.parse(fechaFin);
            LocalDate hoy = LocalDate.now();

            if (fechaInicioDate.isBefore(hoy)) {
                log.warn("⚠️ Fecha de inicio en el pasado: {}", fechaInicio);
                variables.put("datosValidos", false);
                variables.put("motivoRechazo", "La fecha de inicio debe ser futura");
                return variables;
            }

            if (fechaFinDate.isBefore(fechaInicioDate) || fechaFinDate.isEqual(fechaInicioDate)) {
                log.warn("⚠️ Fecha de fin debe ser posterior a fecha de inicio");
                variables.put("datosValidos", false);
                variables.put("motivoRechazo", "La fecha de fin debe ser posterior a la fecha de inicio");
                return variables;
            }

            // 3. Validar monto
            if (monto <= 0) {
                log.warn("⚠️ Monto inválido: {}", monto);
                variables.put("datosValidos", false);
                variables.put("motivoRechazo", "El monto debe ser positivo");
                return variables;
            }

            // 4. Actualizar estado en BD
            ReservaViaje reserva = repositorio.buscarPorId(new ReservaId(reservaId))
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada: " + reservaId));

            reserva.setEstado(EstadoReserva.VALIDANDO_DATOS);
            repositorio.guardar(reserva);

            // 5. Datos válidos
            log.info("✅ Datos validados correctamente para reserva: {}", reservaId);
            variables.put("datosValidos", true);
            variables.put("motivoRechazo", null);

            return variables;

        } catch (Exception e) {
            log.error("❌ Error al validar datos de reserva {}: {}", reservaId, e.getMessage());
            variables.put("datosValidos", false);
            variables.put("motivoRechazo", "Error técnico en validación: " + e.getMessage());
            return variables;
        }
    }
}