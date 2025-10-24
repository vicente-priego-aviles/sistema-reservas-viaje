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
 * Worker de Camunda que notifica al cliente cuando su tarjeta de crédito es inválida.
 *
 * <p>Este worker se ejecuta cuando una tarjeta de crédito no pasa las validaciones
 * necesarias (expirada, datos incorrectos, rechazada por el sistema de pago, etc.)
 * y es necesario informar al cliente para que actualice su información de pago.
 *
 * <p><strong>Funcionalidad:</strong>
 * <ul>
 *   <li>Registra el problema con la tarjeta en logs</li>
 *   <li>Genera mensaje personalizado según el motivo de invalidez</li>
 *   <li>Prepara información para envío de notificación (email/SMS)</li>
 *   <li>Determina urgencia de la notificación</li>
 *   <li>Incluye instrucciones claras para el cliente</li>
 * </ul>
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>{@code clienteId} (String) - ID del cliente (UUID)</li>
 *   <li>{@code tarjetaId} (String, opcional) - ID de la tarjeta inválida</li>
 *   <li>{@code motivoInvalidez} (String) - Motivo por el cual la tarjeta es inválida</li>
 *   <li>{@code numeroEnmascarado} (String, opcional) - Últimos 4 dígitos (**** 1234)</li>
 *   <li>{@code emailCliente} (String, opcional) - Email del cliente</li>
 *   <li>{@code nombreCliente} (String, opcional) - Nombre del cliente</li>
 *   <li>{@code intentosPago} (Integer, opcional) - Número de intentos fallidos</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>{@code notificacionEnviada} (Boolean) - true si se procesó correctamente</li>
 *   <li>{@code tipoNotificacion} (String) - Tipo: EMAIL, SMS, AMBOS</li>
 *   <li>{@code mensajeNotificacion} (String) - Mensaje generado para el cliente</li>
 *   <li>{@code timestampNotificacion} (String) - Timestamp ISO-8601</li>
 *   <li>{@code urgencia} (String) - Nivel de urgencia: ALTA, MEDIA, BAJA</li>
 *   <li>{@code requiereActualizacion} (Boolean) - Si requiere actualizar tarjeta</li>
 *   <li>{@code bloquearReservas} (Boolean) - Si debe bloquear nuevas reservas</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>{@code ERROR_NOTIFICACION_FALLIDA} - Error al procesar la notificación</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> Este worker NO envía directamente el email/SMS.
 * Genera la información necesaria para que un sistema externo de mensajería se encargue.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificarTarjetaInvalidaWorker {

    private static final DateTimeFormatter FORMATO_TIMESTAMP = DateTimeFormatter.ISO_DATE_TIME;

    // Umbrales de intentos
    private static final int UMBRAL_INTENTOS_ALTO = 3;
    private static final int UMBRAL_INTENTOS_CRITICO = 5;

    // Motivos que requieren bloqueo de reservas
    private static final String[] MOTIVOS_BLOQUEO = {
            "FRAUDE",
            "LISTA_NEGRA",
            "ROBADA",
            "BLOQUEADA"
    };

    /**
     * Procesa la notificación de tarjeta inválida.
     *
     * @param job job activado por Camunda
     * @return mapa con variables de salida
     * @throws ZeebeBpmnError si ocurre un error durante el procesamiento
     */
    @JobWorker(type = "notifyInvalidCreditCard", autoComplete = true)
    public Map<String, Object> notificarTarjetaInvalida(ActivatedJob job) {
        log.info("💳 Iniciando notificación de tarjeta inválida - Job: {}", job.getKey());

        try {
            // 1. Extraer variables de entrada
            Map<String, Object> variables = job.getVariablesAsMap();

            String clienteId = extraerClienteId(variables);
            String tarjetaId = extraerCampoOpcional(variables, "tarjetaId");
            String motivoInvalidez = extraerMotivoInvalidez(variables);
            String numeroEnmascarado = extraerCampoOpcional(variables, "numeroEnmascarado");
            String emailCliente = extraerCampoOpcional(variables, "emailCliente");
            String nombreCliente = extraerCampoOpcional(variables, "nombreCliente");
            Integer intentosPago = extraerIntentosPago(variables);

            log.warn("⚠️ Tarjeta inválida detectada - Cliente: {} - Tarjeta: {} - Motivo: {}",
                    clienteId, numeroEnmascarado != null ? numeroEnmascarado : tarjetaId, motivoInvalidez);

            // 2. Determinar urgencia según motivo e intentos
            String urgencia = determinarUrgencia(motivoInvalidez, intentosPago);

            // 3. Determinar si debe bloquear nuevas reservas
            boolean bloquearReservas = debeBloquearReservas(motivoInvalidez, intentosPago);

            if (bloquearReservas) {
                log.error("❌ Se recomienda bloquear reservas para el cliente: {}", clienteId);
            }

            // 4. Generar mensaje personalizado
            String mensajeNotificacion = generarMensajeNotificacion(
                    nombreCliente,
                    numeroEnmascarado,
                    motivoInvalidez,
                    intentosPago,
                    bloquearReservas
            );

            // 5. Determinar tipo de notificación
            String tipoNotificacion = determinarTipoNotificacion(emailCliente, urgencia);

            // 6. Registrar timestamp
            String timestampNotificacion = LocalDateTime.now().format(FORMATO_TIMESTAMP);

            log.info("✅ Notificación de tarjeta inválida procesada - Urgencia: {}", urgencia);

            // 7. Retornar variables de salida
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("notificacionEnviada", true);
            resultado.put("tipoNotificacion", tipoNotificacion);
            resultado.put("mensajeNotificacion", mensajeNotificacion);
            resultado.put("timestampNotificacion", timestampNotificacion);
            resultado.put("urgencia", urgencia);
            resultado.put("requiereActualizacion", true);
            resultado.put("bloquearReservas", bloquearReservas);

            // Variables adicionales para sistema de mensajería
            if (StringUtils.isNotBlank(emailCliente)) {
                resultado.put("emailDestino", emailCliente);
            }

            resultado.put("asuntoEmail", generarAsuntoEmail(urgencia, numeroEnmascarado));
            resultado.put("prioridadNotificacion", urgencia);

            // Contador de intentos para seguimiento
            if (intentosPago != null) {
                resultado.put("intentosPago", intentosPago);
            }

            return resultado;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error en datos de entrada: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_NOTIFICACION_FALLIDA",
                    "Error al procesar notificación de tarjeta inválida: " + e.getMessage(),
                    Map.of("notificacionEnviada", false)
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al notificar tarjeta inválida: {}", e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_NOTIFICACION_FALLIDA",
                    "Error inesperado al procesar notificación: " + e.getMessage(),
                    Map.of("notificacionEnviada", false)
            );
        }
    }

    /**
     * Extrae el ID del cliente de las variables del proceso.
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
     * Extrae el motivo de invalidez de las variables del proceso.
     */
    private String extraerMotivoInvalidez(Map<String, Object> variables) {
        if (!variables.containsKey("motivoInvalidez")) {
            throw new IllegalArgumentException("La variable 'motivoInvalidez' es obligatoria");
        }

        Object motivoObj = variables.get("motivoInvalidez");

        if (motivoObj == null) {
            throw new IllegalArgumentException("El 'motivoInvalidez' no puede ser nulo");
        }

        String motivo = motivoObj.toString().trim();

        if (StringUtils.isBlank(motivo)) {
            throw new IllegalArgumentException("El 'motivoInvalidez' no puede estar vacío");
        }

        return motivo;
    }

    /**
     * Extrae un campo opcional de las variables del proceso.
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
     * Extrae el número de intentos de pago de las variables del proceso.
     */
    private Integer extraerIntentosPago(Map<String, Object> variables) {
        if (!variables.containsKey("intentosPago")) {
            return null;
        }

        Object intentosObj = variables.get("intentosPago");

        if (intentosObj == null) {
            return null;
        }

        try {
            if (intentosObj instanceof Number) {
                return ((Number) intentosObj).intValue();
            } else {
                return Integer.parseInt(intentosObj.toString());
            }
        } catch (NumberFormatException e) {
            log.warn("⚠️ No se pudo parsear intentosPago: {}", intentosObj);
            return null;
        }
    }

    /**
     * Determina el nivel de urgencia de la notificación.
     */
    private String determinarUrgencia(String motivoInvalidez, Integer intentosPago) {
        String motivoUpper = motivoInvalidez.toUpperCase();

        // Urgencia ALTA: fraude, lista negra, robada
        if (motivoUpper.contains("FRAUDE") ||
            motivoUpper.contains("LISTA_NEGRA") ||
            motivoUpper.contains("ROBADA") ||
            motivoUpper.contains("BLOQUEADA")) {
            return "ALTA";
        }

        // Urgencia ALTA: muchos intentos fallidos
        if (intentosPago != null && intentosPago >= UMBRAL_INTENTOS_CRITICO) {
            return "ALTA";
        }

        // Urgencia MEDIA: tarjeta expirada, datos incorrectos, varios intentos
        if (motivoUpper.contains("EXPIRADA") ||
            motivoUpper.contains("DATOS_INCORRECTOS") ||
            motivoUpper.contains("CVV_INVALIDO") ||
            (intentosPago != null && intentosPago >= UMBRAL_INTENTOS_ALTO)) {
            return "MEDIA";
        }

        // Urgencia BAJA: validación fallida, formato incorrecto
        return "BAJA";
    }

    /**
     * Determina si se deben bloquear nuevas reservas.
     */
    private boolean debeBloquearReservas(String motivoInvalidez, Integer intentosPago) {
        String motivoUpper = motivoInvalidez.toUpperCase();

        // Bloquear por motivos de seguridad
        for (String motivoBloqueo : MOTIVOS_BLOQUEO) {
            if (motivoUpper.contains(motivoBloqueo)) {
                return true;
            }
        }

        // Bloquear por demasiados intentos fallidos
        if (intentosPago != null && intentosPago >= UMBRAL_INTENTOS_CRITICO) {
            return true;
        }

        return false;
    }

    /**
     * Genera el mensaje de notificación personalizado.
     */
    private String generarMensajeNotificacion(
            String nombreCliente,
            String numeroEnmascarado,
            String motivoInvalidez,
            Integer intentosPago,
            boolean bloquearReservas
    ) {
        StringBuilder mensaje = new StringBuilder();

        // Saludo personalizado
        if (StringUtils.isNotBlank(nombreCliente)) {
            mensaje.append("Estimado/a ").append(nombreCliente).append(",\n\n");
        } else {
            mensaje.append("Estimado/a cliente,\n\n");
        }

        // Mensaje principal según severidad
        if (bloquearReservas) {
            mensaje.append("Le informamos que hemos detectado un problema crítico con su método de pago");
        } else {
            mensaje.append("Le informamos que su método de pago requiere actualización");
        }

        // Incluir número de tarjeta si está disponible
        if (StringUtils.isNotBlank(numeroEnmascarado)) {
            mensaje.append(" (").append(numeroEnmascarado).append(")");
        }

        mensaje.append(".\n\n");

        // Detalles del problema
        mensaje.append("Motivo: ").append(traducirMotivoInvalidez(motivoInvalidez)).append("\n\n");

        // Información de intentos si aplica
        if (intentosPago != null && intentosPago > 1) {
            mensaje.append("Se han registrado ").append(intentosPago)
                    .append(" intentos fallidos de procesamiento.\n\n");
        }

        // Acciones requeridas
        if (bloquearReservas) {
            mensaje.append("⚠️ IMPORTANTE: Por seguridad, hemos suspendido temporalmente su capacidad ")
                    .append("de realizar nuevas reservas hasta que actualice su información de pago.\n\n");
        }

        mensaje.append("Por favor, actualice su información de pago lo antes posible:\n\n");
        mensaje.append("1. Acceda a su cuenta en nuestro portal web\n");
        mensaje.append("2. Vaya a 'Mi Perfil' > 'Métodos de Pago'\n");
        mensaje.append("3. Actualice o agregue una nueva tarjeta de crédito\n");
        mensaje.append("4. Verifique que la información sea correcta\n\n");

        // Información de contacto
        mensaje.append("Si tiene alguna duda o necesita asistencia, contáctenos:\n");
        mensaje.append("- Email: soporte@reservasviaje.com\n");
        mensaje.append("- Teléfono: +34 900 123 456\n");
        mensaje.append("- Chat en vivo: disponible 24/7 en nuestro sitio web\n\n");

        // Despedida
        mensaje.append("Agradecemos su comprensión.\n\n");
        mensaje.append("Atentamente,\n");
        mensaje.append("Equipo de Reservas de Viaje");

        return mensaje.toString();
    }

    /**
     * Traduce códigos técnicos a mensajes amigables.
     */
    private String traducirMotivoInvalidez(String motivoInvalidez) {
        if (StringUtils.isBlank(motivoInvalidez)) {
            return "Problema con el método de pago";
        }

        String motivoUpper = motivoInvalidez.toUpperCase();

        // Traducciones comunes
        if (motivoUpper.contains("EXPIRADA") || motivoUpper.contains("EXPIRED")) {
            return "La tarjeta ha expirado";
        }

        if (motivoUpper.contains("CVV") || motivoUpper.contains("CODIGO_SEGURIDAD")) {
            return "El código de seguridad (CVV) es incorrecto";
        }

        if (motivoUpper.contains("DATOS_INCORRECTOS") || motivoUpper.contains("INVALID_DATA")) {
            return "Los datos de la tarjeta son incorrectos";
        }

        if (motivoUpper.contains("FONDOS") || motivoUpper.contains("INSUFFICIENT_FUNDS")) {
            return "Fondos insuficientes";
        }

        if (motivoUpper.contains("RECHAZADA") || motivoUpper.contains("DECLINED")) {
            return "La tarjeta fue rechazada por el banco emisor";
        }

        if (motivoUpper.contains("FRAUDE") || motivoUpper.contains("FRAUD")) {
            return "Sospecha de actividad fraudulenta detectada";
        }

        if (motivoUpper.contains("ROBADA") || motivoUpper.contains("STOLEN")) {
            return "La tarjeta fue reportada como robada";
        }

        if (motivoUpper.contains("BLOQUEADA") || motivoUpper.contains("BLOCKED")) {
            return "La tarjeta está bloqueada";
        }

        if (motivoUpper.contains("LIMITE") || motivoUpper.contains("LIMIT")) {
            return "Se ha excedido el límite de crédito";
        }

        // Si no hay traducción, devolver el original
        return motivoInvalidez;
    }

    /**
     * Determina el tipo de notificación según urgencia.
     */
    private String determinarTipoNotificacion(String emailCliente, String urgencia) {
        // Si es urgencia ALTA y hay email, usar EMAIL
        // En el futuro se puede implementar SMS o AMBOS

        if (StringUtils.isNotBlank(emailCliente)) {
            if ("ALTA".equals(urgencia)) {
                return "EMAIL"; // En futuro: "AMBOS" (EMAIL + SMS)
            }
            return "EMAIL";
        }

        return "EMAIL";
    }

    /**
     * Genera el asunto del email.
     */
    private String generarAsuntoEmail(String urgencia, String numeroEnmascarado) {
        StringBuilder asunto = new StringBuilder();

        if ("ALTA".equals(urgencia)) {
            asunto.append("🚨 URGENTE: ");
        } else if ("MEDIA".equals(urgencia)) {
            asunto.append("⚠️ ");
        }

        asunto.append("Problema con su método de pago");

        if (StringUtils.isNotBlank(numeroEnmascarado)) {
            asunto.append(" - ").append(numeroEnmascarado);
        }

        return asunto.toString();
    }
}