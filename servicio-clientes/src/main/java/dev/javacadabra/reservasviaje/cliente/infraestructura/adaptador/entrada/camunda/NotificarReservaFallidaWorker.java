package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker de Camunda que notifica al cliente cuando una reserva ha fallado.
 *
 * <p>Este worker se ejecuta cuando el proceso de reserva falla por cualquier motivo
 * y es necesario informar al cliente sobre el problema.
 *
 * <p><strong>Funcionalidad:</strong>
 * <ul>
 *   <li>Registra el fallo de la reserva en logs</li>
 *   <li>Prepara mensaje de notificación personalizado según el motivo</li>
 *   <li>Genera información para envío de email/SMS (delegado a otro sistema)</li>
 *   <li>Registra timestamp de la notificación</li>
 * </ul>
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>{@code clienteId} (String) - ID del cliente (UUID)</li>
 *   <li>{@code reservaId} (String, opcional) - ID de la reserva fallida</li>
 *   <li>{@code motivoFallo} (String) - Motivo del fallo de la reserva</li>
 *   <li>{@code codigoError} (String, opcional) - Código de error técnico</li>
 *   <li>{@code emailCliente} (String, opcional) - Email del cliente</li>
 *   <li>{@code nombreCliente} (String, opcional) - Nombre del cliente</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>{@code notificacionEnviada} (Boolean) - true si se procesó correctamente</li>
 *   <li>{@code tipoNotificacion} (String) - Tipo: EMAIL, SMS, AMBOS</li>
 *   <li>{@code mensajeNotificacion} (String) - Mensaje generado para el cliente</li>
 *   <li>{@code timestampNotificacion} (String) - Timestamp ISO-8601</li>
 *   <li>{@code requiereContactoManual} (Boolean) - Si requiere seguimiento manual</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>{@code ERROR_NOTIFICACION_FALLIDA} - Error al procesar la notificación</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> Este worker NO envía directamente el email/SMS.
 * Genera la información necesaria para que un sistema externo de mensajería
 * (ejemplo: SendGrid, Twilio) se encargue del envío real.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificarReservaFallidaWorker {

    private static final DateTimeFormatter FORMATO_TIMESTAMP = DateTimeFormatter.ISO_DATE_TIME;

    // Motivos de fallo conocidos que requieren contacto manual
    private static final String[] MOTIVOS_CONTACTO_MANUAL = {
            "ERROR_PAGO_RECHAZADO",
            "ERROR_DISPONIBILIDAD",
            "ERROR_VALIDACION_TARJETA",
            "ERROR_BLOQUEO_FRAUDE"
    };

    /**
     * Procesa la notificación de reserva fallida.
     *
     * @param job job activado por Camunda
     * @return mapa con variables de salida
     * @throws ZeebeBpmnError si ocurre un error durante el procesamiento
     */
    @JobWorker(type = "notificar-reserva-fallida", autoComplete = true)
    public Map<String, Object> notificarReservaFallida(ActivatedJob job) {
        log.info("📨 Iniciando notificación de reserva fallida - Job: {}", job.getKey());

        try {
            // 1. Extraer variables de entrada
            Map<String, Object> variables = job.getVariablesAsMap();

            String clienteId = extraerClienteId(variables);
            String reservaId = extraerCampoOpcional(variables, "reservaId");
            String motivoFallo = extraerMotivoFallo(variables);
            String codigoError = extraerCampoOpcional(variables, "codigoError");
            String emailCliente = extraerCampoOpcional(variables, "emailCliente");
            String nombreCliente = extraerCampoOpcional(variables, "nombreCliente");

            log.info("📧 Notificando fallo de reserva - Cliente: {} - Reserva: {} - Motivo: {}",
                    clienteId, reservaId, motivoFallo);

            // 2. Determinar si requiere contacto manual
            boolean requiereContactoManual = determinarContactoManual(codigoError, motivoFallo);

            if (requiereContactoManual) {
                log.warn("⚠️ La reserva fallida requiere seguimiento manual del equipo de soporte");
            }

            // 3. Generar mensaje personalizado según motivo
            String mensajeNotificacion = generarMensajeNotificacion(
                    nombreCliente,
                    reservaId,
                    motivoFallo,
                    requiereContactoManual
            );

            // 4. Determinar tipo de notificación (EMAIL por defecto)
            String tipoNotificacion = determinarTipoNotificacion(emailCliente);

            // 5. Registrar timestamp de notificación
            String timestampNotificacion = LocalDateTime.now().format(FORMATO_TIMESTAMP);

            log.info("✅ Notificación de reserva fallida procesada correctamente");
            log.debug("📝 Mensaje generado: {}", mensajeNotificacion);

            // 6. Retornar variables de salida
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("notificacionEnviada", true);
            resultado.put("tipoNotificacion", tipoNotificacion);
            resultado.put("mensajeNotificacion", mensajeNotificacion);
            resultado.put("timestampNotificacion", timestampNotificacion);
            resultado.put("requiereContactoManual", requiereContactoManual);

            // Variables adicionales útiles para el sistema de mensajería
            if (StringUtils.isNotBlank(emailCliente)) {
                resultado.put("emailDestino", emailCliente);
            }

            resultado.put("asuntoEmail", generarAsuntoEmail(reservaId));
            resultado.put("prioridadNotificacion", requiereContactoManual ? "ALTA" : "NORMAL");

            return resultado;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error en datos de entrada: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_NOTIFICACION_FALLIDA",
                    "Error al procesar notificación: " + e.getMessage(),
                    Map.of("notificacionEnviada", false)
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al notificar reserva fallida: {}", e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_NOTIFICACION_FALLIDA",
                    "Error inesperado al procesar notificación: " + e.getMessage(),
                    Map.of("notificacionEnviada", false)
            );
        }
    }

    /**
     * Extrae el ID del cliente de las variables del proceso.
     *
     * @param variables variables del proceso
     * @return ID del cliente
     * @throws IllegalArgumentException si el clienteId no está presente o es inválido
     */
    private String extraerClienteId(Map<String, Object> variables) {
        if (!variables.containsKey("clienteId")) {
            throw new IllegalArgumentException("La variable 'clienteId' es obligatoria");
        }

        Object clienteIdObj = variables.get("clienteId");

        if (clienteIdObj == null) {
            throw new IllegalArgumentException("El 'clienteId' no puede ser nulo");
        }

        String clienteId = clienteIdObj.toString().trim();

        if (StringUtils.isBlank(clienteId)) {
            throw new IllegalArgumentException("El 'clienteId' no puede estar vacío");
        }

        return clienteId;
    }

    /**
     * Extrae el motivo del fallo de las variables del proceso.
     *
     * @param variables variables del proceso
     * @return motivo del fallo
     * @throws IllegalArgumentException si el motivoFallo no está presente o es inválido
     */
    private String extraerMotivoFallo(Map<String, Object> variables) {
        if (!variables.containsKey("motivoFallo")) {
            throw new IllegalArgumentException("La variable 'motivoFallo' es obligatoria");
        }

        Object motivoObj = variables.get("motivoFallo");

        if (motivoObj == null) {
            throw new IllegalArgumentException("El 'motivoFallo' no puede ser nulo");
        }

        String motivo = motivoObj.toString().trim();

        if (StringUtils.isBlank(motivo)) {
            throw new IllegalArgumentException("El 'motivoFallo' no puede estar vacío");
        }

        return motivo;
    }

    /**
     * Extrae un campo opcional de las variables del proceso.
     *
     * @param variables variables del proceso
     * @param nombreCampo nombre del campo a extraer
     * @return valor del campo o null si no está presente
     */
    private String extraerCampoOpcional(Map<String, Object> variables, String nombreCampo) {
        if (!variables.containsKey(nombreCampo)) {
            return null;
        }

        Object valor = variables.get(nombreCampo);

        if (valor == null) {
            return null;
        }

        String valorStr = valor.toString().trim();

        return StringUtils.isBlank(valorStr) ? null : valorStr;
    }

    /**
     * Determina si el fallo requiere contacto manual del equipo de soporte.
     *
     * @param codigoError código de error técnico
     * @param motivoFallo motivo del fallo
     * @return true si requiere contacto manual
     */
    private boolean determinarContactoManual(String codigoError, String motivoFallo) {
        // Verificar código de error
        if (StringUtils.isNotBlank(codigoError)) {
            for (String motivoManual : MOTIVOS_CONTACTO_MANUAL) {
                if (codigoError.contains(motivoManual)) {
                    return true;
                }
            }
        }

        // Verificar motivo de fallo
        if (StringUtils.isNotBlank(motivoFallo)) {
            String motivoUpper = motivoFallo.toUpperCase();

            // Palabras clave que indican necesidad de contacto manual
            if (motivoUpper.contains("FRAUDE") ||
                motivoUpper.contains("BLOQUEADO") ||
                motivoUpper.contains("RECHAZADO") ||
                motivoUpper.contains("TARJETA INVALIDA")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Genera el mensaje de notificación personalizado para el cliente.
     *
     * @param nombreCliente nombre del cliente (puede ser null)
     * @param reservaId ID de la reserva (puede ser null)
     * @param motivoFallo motivo del fallo
     * @param requiereContactoManual si requiere seguimiento manual
     * @return mensaje personalizado
     */
    private String generarMensajeNotificacion(
            String nombreCliente,
            String reservaId,
            String motivoFallo,
            boolean requiereContactoManual
    ) {
        StringBuilder mensaje = new StringBuilder();

        // Saludo personalizado
        if (StringUtils.isNotBlank(nombreCliente)) {
            mensaje.append("Estimado/a ").append(nombreCliente).append(",\n\n");
        } else {
            mensaje.append("Estimado/a cliente,\n\n");
        }

        // Mensaje principal
        mensaje.append("Lamentamos informarle que su reserva");

        if (StringUtils.isNotBlank(reservaId)) {
            mensaje.append(" (").append(reservaId).append(")");
        }

        mensaje.append(" no ha podido ser procesada.\n\n");

        // Motivo del fallo (en lenguaje amigable)
        mensaje.append("Motivo: ").append(traducirMotivoFallo(motivoFallo)).append("\n\n");

        // Siguiente paso según requiera contacto manual o no
        if (requiereContactoManual) {
            mensaje.append("Nuestro equipo de atención al cliente se pondrá en contacto con usted ")
                    .append("en las próximas 24 horas para resolver esta situación.\n\n");
            mensaje.append("Si desea contactarnos antes, puede hacerlo a través de:\n");
            mensaje.append("- Email: soporte@reservasviaje.com\n");
            mensaje.append("- Teléfono: +34 900 123 456\n\n");
        } else {
            mensaje.append("Por favor, revise los datos proporcionados e intente nuevamente.\n\n");
            mensaje.append("Si el problema persiste, no dude en contactarnos:\n");
            mensaje.append("- Email: soporte@reservasviaje.com\n");
            mensaje.append("- Teléfono: +34 900 123 456\n\n");
        }

        // Despedida
        mensaje.append("Disculpe las molestias ocasionadas.\n\n");
        mensaje.append("Atentamente,\n");
        mensaje.append("Equipo de Reservas de Viaje");

        return mensaje.toString();
    }

    /**
     * Traduce códigos de error técnicos a mensajes amigables para el cliente.
     *
     * @param motivoFallo motivo técnico del fallo
     * @return mensaje amigable para el cliente
     */
    private String traducirMotivoFallo(String motivoFallo) {
        if (StringUtils.isBlank(motivoFallo)) {
            return "Error al procesar la reserva";
        }

        String motivoUpper = motivoFallo.toUpperCase();

        // Traducir códigos técnicos comunes
        if (motivoUpper.contains("PAGO") || motivoUpper.contains("PAYMENT")) {
            return "Problema con el método de pago";
        }

        if (motivoUpper.contains("TARJETA") || motivoUpper.contains("CARD")) {
            return "Información de tarjeta inválida o expirada";
        }

        if (motivoUpper.contains("DISPONIBILIDAD") || motivoUpper.contains("AVAILABILITY")) {
            return "No hay disponibilidad para las fechas seleccionadas";
        }

        if (motivoUpper.contains("VALIDACION") || motivoUpper.contains("VALIDATION")) {
            return "Algunos datos proporcionados no son válidos";
        }

        if (motivoUpper.contains("FRAUDE") || motivoUpper.contains("FRAUD")) {
            return "Se requiere verificación adicional de seguridad";
        }

        if (motivoUpper.contains("TIMEOUT") || motivoUpper.contains("TIEMPO")) {
            return "Se agotó el tiempo de espera, por favor intente nuevamente";
        }

        if (motivoUpper.contains("CONEXION") || motivoUpper.contains("CONNECTION")) {
            return "Error temporal de conexión con el sistema";
        }

        // Si no hay traducción específica, devolver el motivo original
        // (probablemente ya esté en lenguaje amigable)
        return motivoFallo;
    }

    /**
     * Determina el tipo de notificación a enviar.
     *
     * @param emailCliente email del cliente (puede ser null)
     * @return tipo de notificación: EMAIL, SMS o AMBOS
     */
    private String determinarTipoNotificacion(String emailCliente) {
        // Por ahora, solo EMAIL
        // En el futuro se puede implementar lógica para SMS o ambos

        if (StringUtils.isNotBlank(emailCliente)) {
            return "EMAIL";
        }

        // Si no hay email, se podría intentar SMS
        // Pero para este caso devolvemos EMAIL por defecto
        return "EMAIL";
    }

    /**
     * Genera el asunto del email de notificación.
     *
     * @param reservaId ID de la reserva (puede ser null)
     * @return asunto del email
     */
    private String generarAsuntoEmail(String reservaId) {
        if (StringUtils.isNotBlank(reservaId)) {
            return "Reserva " + reservaId + " - Problema al procesar";
        }

        return "Problema al procesar su reserva";
    }
}