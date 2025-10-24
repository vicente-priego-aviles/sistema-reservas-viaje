package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.TarjetaCreditoDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.ConsultarClienteUseCase;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Worker de Camunda para validar la tarjeta de crédito de un cliente.
 *
 * <p>Este worker realiza una validación completa de la tarjeta de crédito del cliente,
 * verificando tanto criterios locales (fecha de expiración) como simulando una validación
 * con una pasarela de pago externa.
 *
 * <p><strong>Job Type:</strong> {@code validar-tarjeta-credito}
 *
 * <p><strong>BPMN:</strong> {@code subproceso-gestion-cliente.bpmn}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>clienteId (String): Identificador único del cliente</li>
 *   <li>tarjetaId (String, opcional): ID de tarjeta específica a validar.
 *       Si no se proporciona, se valida la primera tarjeta válida del cliente</li>
 *   <li>montoReserva (Double, opcional): Monto de la reserva para validar saldo</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>tarjetaValida (Boolean): true si la tarjeta pasó todas las validaciones</li>
 *   <li>tarjetaId (String): ID de la tarjeta validada</li>
 *   <li>tipoTarjeta (String): Tipo de tarjeta (VISA, MASTERCARD, etc.)</li>
 *   <li>ultimosCuatroDigitos (String): Últimos 4 dígitos de la tarjeta</li>
 *   <li>codigoAutorizacion (String): Código de autorización simulado de la pasarela</li>
 *   <li>fechaValidacion (String): Timestamp de la validación en formato ISO</li>
 *   <li>motivoRechazo (String, opcional): Motivo del rechazo si la validación falla</li>
 * </ul>
 *
 * <p><strong>Validaciones realizadas:</strong>
 * <ol>
 *   <li>El cliente debe existir en el sistema</li>
 *   <li>El cliente debe tener al menos una tarjeta registrada</li>
 *   <li>La tarjeta debe existir y pertenecer al cliente</li>
 *   <li>La tarjeta no debe estar expirada</li>
 *   <li>Simulación de validación con pasarela de pago externa</li>
 *   <li>Validación de saldo disponible (simulado)</li>
 * </ol>
 *
 * <p><strong>Comportamiento en caso de error:</strong>
 * <ul>
 *   <li>Si la validación falla: lanza ZeebeBpmnError con código ERROR_TARJETA_INVALIDA</li>
 *   <li>El Boundary Event en el BPMN captura este error y redirige al flujo de error</li>
 * </ul>
 *
 * <p><strong>Simulación de Pasarela de Pago:</strong>
 * <p>Se simula una validación con pasarela de pago usando las siguientes reglas:
 * <ul>
 *   <li>Tarjetas terminadas en 0000: Siempre rechazadas (saldo insuficiente)</li>
 *   <li>Tarjetas terminadas en 9999: Siempre rechazadas (tarjeta bloqueada)</li>
 *   <li>Tarjetas terminadas en 6666: Siempre rechazadas (tarjeta fraudulenta)</li>
 *   <li>Resto de tarjetas: Aprobadas con código de autorización simulado</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 * @see ConsultarClienteUseCase
 * @see TarjetaCreditoDTO
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidarTarjetaCreditoWorker {

    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final Random random = new Random();

    /**
     * Maneja el job de validar tarjeta de crédito desde Camunda.
     *
     * <p>Realiza una validación completa de la tarjeta, incluyendo fecha de expiración
     * y simulación de validación con pasarela de pago. Si la validación falla, lanza
     * un error BPMN que será capturado por el Boundary Event.
     *
     * @param job job activado por Zeebe con las variables del proceso
     * @return mapa con las variables de salida para el proceso BPMN
     * @throws ZeebeBpmnError si la tarjeta es inválida o hay error en la validación
     */
    @JobWorker(type = "validar-tarjeta-credito", autoComplete = true)
    public Map<String, Object> manejarValidarTarjetaCredito(ActivatedJob job) {
        log.info("🚀 Iniciando worker validar-tarjeta-credito - Job Key: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        log.debug("🔍 Variables recibidas: {}", variables);

        try {
            // 1. Extraer variables de entrada
            String clienteId = extraerClienteId(variables);
            String tarjetaIdEspecifica = extraerTarjetaId(variables);
            Double montoReserva = extraerMontoReserva(variables);

            log.info("💳 Validando tarjeta de crédito para cliente: {}", clienteId);

            // 2. Obtener cliente y sus tarjetas
            ClienteDTO cliente = obtenerCliente(clienteId);

            // 3. Validar que el cliente tenga tarjetas
            validarClienteTieneTarjetas(cliente);

            // 4. Seleccionar tarjeta a validar
            TarjetaCreditoDTO tarjeta = seleccionarTarjeta(cliente, tarjetaIdEspecifica);

            log.info("🔍 Tarjeta seleccionada: {} - Tipo: {} - Últimos 4 dígitos: {}",
                    tarjeta.tarjetaId(),
                    tarjeta.tipoTarjeta(),
                    obtenerUltimosCuatroDigitos(tarjeta.numeroEnmascarado()));

            // 5. Validar fecha de expiración
            validarFechaExpiracion(tarjeta);

            // 6. Simular validación con pasarela de pago
            ResultadoValidacionPasarela resultado = simularValidacionPasarela(
                    tarjeta,
                    montoReserva
            );

            if (!resultado.aprobada()) {
                log.error("❌ Tarjeta rechazada por pasarela de pago: {}",
                        resultado.motivoRechazo());

                throw new ZeebeBpmnError(
                        "ERROR_TARJETA_INVALIDA",
                        "Tarjeta rechazada: " + resultado.motivoRechazo(),
                        Map.of(
                                "tarjetaId", tarjeta.tarjetaId(),
                                "motivoRechazo", resultado.motivoRechazo(),
                                "codigoRechazo", resultado.codigoRespuesta()
                        )
                );
            }

            log.info("✅ Tarjeta validada correctamente - Código autorización: {}",
                    resultado.codigoAutorizacion());

            // 7. Preparar variables de salida
            Map<String, Object> output = construirOutputExitoso(tarjeta, resultado);

            log.info("📤 Validación completada exitosamente");

            return output;

        } catch (ZeebeBpmnError e) {
            // Re-lanzar errores BPMN para que sean capturados por Boundary Event
            throw e;

        } catch (ClienteNoEncontradoExcepcion e) {
            log.error("❌ Cliente no encontrado: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_TARJETA_INVALIDA",
                    "Cliente no encontrado: " + e.getMessage(),
                    Map.of("motivoRechazo", "Cliente no existe en el sistema")
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al validar tarjeta: {}", e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_TARJETA_INVALIDA",
                    "Error al validar tarjeta: " + e.getMessage(),
                    Map.of(
                            "errorType", "ERROR_SISTEMA",
                            "mensajeError", e.getMessage()
                    )
            );
        }
    }

    private String extraerClienteId(Map<String, Object> variables) {
        Object clienteIdObj = variables.get("clienteId");
        if (clienteIdObj == null) {
            throw new IllegalArgumentException("La variable 'clienteId' es obligatoria");
        }
        return clienteIdObj.toString().trim();
    }

    private String extraerTarjetaId(Map<String, Object> variables) {
        Object tarjetaIdObj = variables.get("tarjetaId");
        return tarjetaIdObj != null ? tarjetaIdObj.toString().trim() : null;
    }

    private Double extraerMontoReserva(Map<String, Object> variables) {
        Object montoObj = variables.get("montoReserva");
        if (montoObj == null) {
            log.debug("⚠️ No se proporcionó montoReserva, usando monto por defecto para validación");
            return 1000.0;
        }
        if (montoObj instanceof Number) {
            return ((Number) montoObj).doubleValue();
        }
        return Double.parseDouble(montoObj.toString());
    }

    private ClienteDTO obtenerCliente(String clienteId) {
        return consultarClienteUseCase.buscarPorId(clienteId);
    }

    private void validarClienteTieneTarjetas(ClienteDTO cliente) {
        if (cliente.cantidadTarjetas() == 0) {
            log.error("❌ El cliente {} no tiene tarjetas registradas", cliente.clienteId());
            throw new ZeebeBpmnError(
                    "ERROR_TARJETA_INVALIDA",
                    "El cliente no tiene tarjetas de crédito registradas",
                    Map.of("motivoRechazo", "Sin tarjetas registradas")
            );
        }
        if (!cliente.tieneTarjetasValidas()) {
            log.error("❌ El cliente {} no tiene tarjetas válidas", cliente.clienteId());
            throw new ZeebeBpmnError(
                    "ERROR_TARJETA_INVALIDA",
                    "El cliente no tiene tarjetas válidas (todas expiradas)",
                    Map.of("motivoRechazo", "Todas las tarjetas están expiradas")
            );
        }
        log.debug("✅ Cliente tiene {} tarjeta(s) registrada(s)", cliente.cantidadTarjetas());
    }

    private TarjetaCreditoDTO seleccionarTarjeta(ClienteDTO cliente, String tarjetaIdEspecifica) {
        if (StringUtils.isNotBlank(tarjetaIdEspecifica)) {
            return cliente.tarjetas().stream()
                    .filter(t -> t.tarjetaId().equals(tarjetaIdEspecifica))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("❌ Tarjeta {} no encontrada para el cliente {}",
                                tarjetaIdEspecifica, cliente.clienteId());
                        return new ZeebeBpmnError(
                                "ERROR_TARJETA_INVALIDA",
                                "Tarjeta especificada no encontrada",
                                Map.of("motivoRechazo", "Tarjeta no pertenece al cliente")
                        );
                    });
        } else {
            return cliente.tarjetas().stream()
                    .filter(TarjetaCreditoDTO::esValida)
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("❌ No hay tarjetas válidas para el cliente {}", cliente.clienteId());
                        return new ZeebeBpmnError(
                                "ERROR_TARJETA_INVALIDA",
                                "No hay tarjetas válidas disponibles",
                                Map.of("motivoRechazo", "Todas las tarjetas expiradas")
                        );
                    });
        }
    }

    private void validarFechaExpiracion(TarjetaCreditoDTO tarjeta) {
        YearMonth expiracion = YearMonth.of(
                tarjeta.fechaExpiracion().getYear(),
                tarjeta.fechaExpiracion().getMonthValue()
        );
        YearMonth ahora = YearMonth.now();

        if (expiracion.isBefore(ahora)) {
            log.error("❌ Tarjeta expirada: {} - Expiró en: {}/{}",
                    tarjeta.tarjetaId(),
                    expiracion.getMonthValue(),
                    expiracion.getYear());
            throw new ZeebeBpmnError(
                    "ERROR_TARJETA_INVALIDA",
                    String.format("Tarjeta expirada (venció en %02d/%d)",
                            expiracion.getMonthValue(), expiracion.getYear()),
                    Map.of(
                            "motivoRechazo", "Tarjeta expirada",
                            "fechaExpiracion", expiracion.toString()
                    )
            );
        }
        log.debug("✅ Fecha de expiración válida: {}/{}",
                expiracion.getMonthValue(), expiracion.getYear());
    }

    private ResultadoValidacionPasarela simularValidacionPasarela(
            TarjetaCreditoDTO tarjeta, Double monto) {
        log.info("🔐 Simulando validación con pasarela de pago - Monto: {}€", monto);

        try {
            Thread.sleep(100 + random.nextInt(400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String ultimosCuatro = obtenerUltimosCuatroDigitos(tarjeta.numeroEnmascarado());

        if ("0000".equals(ultimosCuatro)) {
            log.warn("⚠️ Pasarela de pago: Saldo insuficiente");
            return ResultadoValidacionPasarela.rechazada(
                    "INSUFFICIENT_FUNDS", "Saldo insuficiente en la tarjeta");
        }
        if ("9999".equals(ultimosCuatro)) {
            log.warn("⚠️ Pasarela de pago: Tarjeta bloqueada");
            return ResultadoValidacionPasarela.rechazada(
                    "CARD_BLOCKED", "La tarjeta está bloqueada por el banco emisor");
        }
        if ("6666".equals(ultimosCuatro)) {
            log.warn("⚠️ Pasarela de pago: Sospecha de fraude");
            return ResultadoValidacionPasarela.rechazada(
                    "FRAUD_SUSPECTED", "Transacción rechazada por sospecha de fraude");
        }

        String codigoAutorizacion = generarCodigoAutorizacion();
        log.info("✅ Pasarela de pago: Transacción APROBADA - Código: {}", codigoAutorizacion);
        return ResultadoValidacionPasarela.aprobada(codigoAutorizacion);
    }

    private String obtenerUltimosCuatroDigitos(String numeroEnmascarado) {
        if (StringUtils.isBlank(numeroEnmascarado) || numeroEnmascarado.length() < 4) {
            return "0000";
        }
        return numeroEnmascarado.substring(numeroEnmascarado.length() - 4);
    }

    private String generarCodigoAutorizacion() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Map<String, Object> construirOutputExitoso(
            TarjetaCreditoDTO tarjeta, ResultadoValidacionPasarela resultado) {
        Map<String, Object> output = new HashMap<>();
        output.put("tarjetaValida", true);
        output.put("tarjetaId", tarjeta.tarjetaId());
        output.put("tipoTarjeta", tarjeta.nombreTipoTarjeta());
        output.put("ultimosCuatroDigitos", obtenerUltimosCuatroDigitos(tarjeta.numeroEnmascarado()));
        output.put("codigoAutorizacion", resultado.codigoAutorizacion());
        output.put("fechaValidacion", LocalDateTime.now().toString());
        output.put("fechaExpiracionTarjeta",
                String.format("%02d/%d",
                        tarjeta.fechaExpiracion().getMonthValue(),
                        tarjeta.fechaExpiracion().getYear()));
        log.debug("✅ Output construido con {} variables", output.size());
        return output;
    }

    private record ResultadoValidacionPasarela(
            boolean aprobada,
            String codigoRespuesta,
            String codigoAutorizacion,
            String motivoRechazo) {

        static ResultadoValidacionPasarela aprobada(String codigoAutorizacion) {
            return new ResultadoValidacionPasarela(true, "APPROVED", codigoAutorizacion, null);
        }

        static ResultadoValidacionPasarela rechazada(String codigoRespuesta, String motivo) {
            return new ResultadoValidacionPasarela(false, codigoRespuesta, null, motivo);
        }
    }
}