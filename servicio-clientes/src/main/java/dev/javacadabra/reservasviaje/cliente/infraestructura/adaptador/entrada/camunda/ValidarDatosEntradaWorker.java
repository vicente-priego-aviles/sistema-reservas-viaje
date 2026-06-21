package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.exception.BpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Worker de Camunda que valida los datos de entrada de una reserva.
 *
 * <p>Este worker realiza validaciones exhaustivas de formato y coherencia
 * sobre los datos proporcionados al iniciar una reserva de viaje.
 *
 * <p><strong>Validaciones realizadas:</strong>
 * <ul>
 *   <li>ClienteId existe y es válido (formato UUID)</li>
 *   <li>Fechas son válidas y coherentes (fechaFin > fechaInicio)</li>
 *   <li>Fechas no están en el pasado</li>
 *   <li>Destino no está vacío y tiene longitud válida</li>
 *   <li>Número de pasajeros está en rango válido (1-10)</li>
 *   <li>Email tiene formato válido</li>
 *   <li>Teléfono tiene formato válido (internacional o local)</li>
 * </ul>
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>{@code clienteId} (String) - ID del cliente (UUID)</li>
 *   <li>{@code fechaInicio} (String) - Fecha inicio reserva (yyyy-MM-dd)</li>
 *   <li>{@code fechaFin} (String) - Fecha fin reserva (yyyy-MM-dd)</li>
 *   <li>{@code destino} (String) - Destino del viaje</li>
 *   <li>{@code numeroPasajeros} (Integer) - Número de pasajeros</li>
 *   <li>{@code emailContacto} (String) - Email de contacto</li>
 *   <li>{@code telefonoContacto} (String) - Teléfono de contacto</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>{@code datosValidos} (Boolean) - true si todos los datos son válidos</li>
 *   <li>{@code erroresValidacion} (List&lt;String&gt;) - Lista de errores encontrados</li>
 *   <li>{@code advertencias} (List&lt;String&gt;) - Lista de advertencias (no bloquean)</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>{@code ERROR_DATOS_INVALIDOS} - Datos de entrada inválidos</li>
 *   <li>{@code ERROR_VALIDACION} - Error durante el proceso de validación</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidarDatosEntradaWorker {

    // Patrones de validación
    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PATRON_TELEFONO = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$" // Formato E.164 internacional
    );

    private static final Pattern PATRON_UUID = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Constantes de validación
    private static final int MIN_PASAJEROS = 1;
    private static final int MAX_PASAJEROS = 10;
    private static final int MIN_LONGITUD_DESTINO = 2;
    private static final int MAX_LONGITUD_DESTINO = 100;
    private static final int MAX_DIAS_ADELANTO = 365; // Máximo 1 año de adelanto

    /**
     * Valida los datos de entrada de una reserva.
     *
     * @param job job activado por Camunda
     * @return mapa con variables de salida
     * @throws BpmnError si los datos son inválidos
     */
    @JobWorker(type = "validar-datos-entrada", autoComplete = true)
    public Map<String, Object> validarDatosEntrada(ActivatedJob job) {
        log.info("✅ Iniciando validación de datos de entrada - Job: {}", job.getKey());

        try {
            // 1. Extraer variables de entrada
            Map<String, Object> variables = job.getVariablesAsMap();

            log.debug("🔍 Variables recibidas: {}", variables.keySet());

            // 2. Realizar validaciones
            List<String> errores = new ArrayList<>();
            List<String> advertencias = new ArrayList<>();

            // Validar ClienteId
            String clienteId = validarClienteId(variables, errores);

            // Validar fechas
            LocalDate fechaInicio = validarFechaInicio(variables, errores);
            LocalDate fechaFin = validarFechaFin(variables, errores);

            // Validar coherencia de fechas
            if (fechaInicio != null && fechaFin != null) {
                validarCoherenciaFechas(fechaInicio, fechaFin, errores, advertencias);
            }

            // Validar destino
            validarDestino(variables, errores);

            // Validar número de pasajeros
            validarNumeroPasajeros(variables, errores, advertencias);

            // Validar email de contacto
            validarEmailContacto(variables, errores);

            // Validar teléfono de contacto
            validarTelefonoContacto(variables, errores);

            // 3. Determinar si los datos son válidos
            boolean datosValidos = errores.isEmpty();

            if (datosValidos) {
                log.info("✅ Datos de entrada válidos - Cliente: {}", clienteId);

                if (!advertencias.isEmpty()) {
                    log.warn("⚠️ Se encontraron {} advertencias", advertencias.size());
                    advertencias.forEach(adv -> log.warn("⚠️ {}", adv));
                }
            } else {
                log.error("❌ Datos de entrada inválidos - {} errores encontrados", errores.size());
                errores.forEach(err -> log.error("❌ {}", err));

                // Lanzar error BPMN con detalles
                throw BpmnError.bpmnError(
                        "ERROR_DATOS_INVALIDOS",
                        "Los datos de entrada contienen errores: " + String.join(", ", errores),
                        Map.of(
                                "datosValidos", false,
                                "erroresValidacion", errores,
                                "advertencias", advertencias
                        )
                );
            }

            // 4. Retornar variables de salida
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("datosValidos", datosValidos);
            resultado.put("erroresValidacion", errores);
            resultado.put("advertencias", advertencias);

            return resultado;

        } catch (BpmnError e) {
            // Propagar errores BPMN
            throw e;

        } catch (Exception e) {
            log.error("❌ Error inesperado durante validación: {}", e.getMessage(), e);
            throw BpmnError.bpmnError(
                    "ERROR_VALIDACION",
                    "Error inesperado durante validación: " + e.getMessage(),
                    Map.of(
                            "datosValidos", false,
                            "erroresValidacion", List.of("Error interno del sistema"),
                            "advertencias", List.of()
                    )
            );
        }
    }

    /**
     * Valida el ID del cliente.
     *
     * @param variables variables del proceso
     * @param errores   lista para acumular errores
     * @return clienteId validado o null si es inválido
     */
    private String validarClienteId(Map<String, Object> variables, List<String> errores) {
        if (!variables.containsKey("clienteId")) {
            errores.add("El campo 'clienteId' es obligatorio");
            return null;
        }

        Object clienteIdObj = variables.get("clienteId");

        if (clienteIdObj == null) {
            errores.add("El 'clienteId' no puede ser nulo");
            return null;
        }

        String clienteId = clienteIdObj.toString().trim();

        if (StringUtils.isBlank(clienteId)) {
            errores.add("El 'clienteId' no puede estar vacío");
            return null;
        }

        // Validar formato UUID
        if (!PATRON_UUID.matcher(clienteId).matches()) {
            errores.add("El 'clienteId' debe ser un UUID válido");
            return null;
        }

        return clienteId;
    }

    /**
     * Valida la fecha de inicio.
     *
     * @param variables variables del proceso
     * @param errores   lista para acumular errores
     * @return fecha de inicio validada o null si es inválida
     */
    private LocalDate validarFechaInicio(Map<String, Object> variables, List<String> errores) {
        if (!variables.containsKey("fechaInicio")) {
            errores.add("El campo 'fechaInicio' es obligatorio");
            return null;
        }

        Object fechaInicioObj = variables.get("fechaInicio");

        if (fechaInicioObj == null) {
            errores.add("La 'fechaInicio' no puede ser nula");
            return null;
        }

        String fechaInicioStr = fechaInicioObj.toString().trim();

        try {
            LocalDate fechaInicio = LocalDate.parse(fechaInicioStr, FORMATO_FECHA);

            // Validar que no esté en el pasado
            if (fechaInicio.isBefore(LocalDate.now())) {
                errores.add("La 'fechaInicio' no puede estar en el pasado");
                return null;
            }

            return fechaInicio;

        } catch (DateTimeParseException e) {
            errores.add("El formato de 'fechaInicio' es inválido. Use yyyy-MM-dd (ejemplo: 2025-12-15)");
            return null;
        }
    }

    /**
     * Valida la fecha de fin.
     *
     * @param variables variables del proceso
     * @param errores   lista para acumular errores
     * @return fecha de fin validada o null si es inválida
     */
    private LocalDate validarFechaFin(Map<String, Object> variables, List<String> errores) {
        if (!variables.containsKey("fechaFin")) {
            errores.add("El campo 'fechaFin' es obligatorio");
            return null;
        }

        Object fechaFinObj = variables.get("fechaFin");

        if (fechaFinObj == null) {
            errores.add("La 'fechaFin' no puede ser nula");
            return null;
        }

        String fechaFinStr = fechaFinObj.toString().trim();

        try {
            LocalDate fechaFin = LocalDate.parse(fechaFinStr, FORMATO_FECHA);

            // Validar que no esté en el pasado
            if (fechaFin.isBefore(LocalDate.now())) {
                errores.add("La 'fechaFin' no puede estar en el pasado");
                return null;
            }

            return fechaFin;

        } catch (DateTimeParseException e) {
            errores.add("El formato de 'fechaFin' es inválido. Use yyyy-MM-dd (ejemplo: 2025-12-22)");
            return null;
        }
    }

    /**
     * Valida la coherencia entre fechas de inicio y fin.
     *
     * @param fechaInicio  fecha de inicio
     * @param fechaFin     fecha de fin
     * @param errores      lista para acumular errores
     * @param advertencias lista para acumular advertencias
     */
    private void validarCoherenciaFechas(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<String> errores,
            List<String> advertencias
    ) {
        // Validar que fechaFin sea posterior a fechaInicio
        if (!fechaFin.isAfter(fechaInicio)) {
            errores.add("La 'fechaFin' debe ser posterior a la 'fechaInicio'");
            return;
        }

        // Calcular duración del viaje
        long duracionDias = java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFin);

        // Advertencia si el viaje es muy corto (1 día)
        if (duracionDias == 1) {
            advertencias.add("El viaje tiene una duración de solo 1 día");
        }

        // Advertencia si el viaje es muy largo (más de 30 días)
        if (duracionDias > 30) {
            advertencias.add("El viaje tiene una duración superior a 30 días (" + duracionDias + " días)");
        }

        // Validar que no sea con demasiado adelanto
        long diasHastaViaje = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaInicio);

        if (diasHastaViaje > MAX_DIAS_ADELANTO) {
            advertencias.add("La reserva se está haciendo con más de 1 año de adelanto");
        }
    }

    /**
     * Valida el destino.
     *
     * @param variables variables del proceso
     * @param errores   lista para acumular errores
     */
    private void validarDestino(Map<String, Object> variables, List<String> errores) {
        if (!variables.containsKey("destino")) {
            errores.add("El campo 'destino' es obligatorio");
            return;
        }

        Object destinoObj = variables.get("destino");

        if (destinoObj == null) {
            errores.add("El 'destino' no puede ser nulo");
            return;
        }

        String destino = destinoObj.toString().trim();

        if (StringUtils.isBlank(destino)) {
            errores.add("El 'destino' no puede estar vacío");
            return;
        }

        if (destino.length() < MIN_LONGITUD_DESTINO) {
            errores.add("El 'destino' debe tener al menos " + MIN_LONGITUD_DESTINO + " caracteres");
            return;
        }

        if (destino.length() > MAX_LONGITUD_DESTINO) {
            errores.add("El 'destino' no puede superar " + MAX_LONGITUD_DESTINO + " caracteres");
        }
    }

    /**
     * Valida el número de pasajeros.
     *
     * @param variables    variables del proceso
     * @param errores      lista para acumular errores
     * @param advertencias lista para acumular advertencias
     */
    private void validarNumeroPasajeros(
            Map<String, Object> variables,
            List<String> errores,
            List<String> advertencias
    ) {
        if (!variables.containsKey("numeroPasajeros")) {
            errores.add("El campo 'numeroPasajeros' es obligatorio");
            return;
        }

        Object numeroPasajerosObj = variables.get("numeroPasajeros");

        if (numeroPasajerosObj == null) {
            errores.add("El 'numeroPasajeros' no puede ser nulo");
            return;
        }

        try {
            int numeroPasajeros;

            if (numeroPasajerosObj instanceof Number) {
                numeroPasajeros = ((Number) numeroPasajerosObj).intValue();
            } else {
                numeroPasajeros = Integer.parseInt(numeroPasajerosObj.toString());
            }

            if (numeroPasajeros < MIN_PASAJEROS) {
                errores.add("El 'numeroPasajeros' debe ser al menos " + MIN_PASAJEROS);
                return;
            }

            if (numeroPasajeros > MAX_PASAJEROS) {
                errores.add("El 'numeroPasajeros' no puede superar " + MAX_PASAJEROS);
                return;
            }

            // Advertencia para grupos grandes
            if (numeroPasajeros >= 5) {
                advertencias.add("Reserva para grupo grande (" + numeroPasajeros + " pasajeros)");
            }

        } catch (NumberFormatException e) {
            errores.add("El 'numeroPasajeros' debe ser un número entero válido");
        }
    }

    /**
     * Valida el email de contacto.
     *
     * @param variables variables del proceso
     * @param errores   lista para acumular errores
     */
    private void validarEmailContacto(Map<String, Object> variables, List<String> errores) {
        if (!variables.containsKey("emailContacto")) {
            errores.add("El campo 'emailContacto' es obligatorio");
            return;
        }

        Object emailObj = variables.get("emailContacto");

        if (emailObj == null) {
            errores.add("El 'emailContacto' no puede ser nulo");
            return;
        }

        String email = emailObj.toString().trim();

        if (StringUtils.isBlank(email)) {
            errores.add("El 'emailContacto' no puede estar vacío");
            return;
        }

        if (!PATRON_EMAIL.matcher(email).matches()) {
            errores.add("El 'emailContacto' tiene un formato inválido");
        }
    }

    /**
     * Valida el teléfono de contacto.
     *
     * @param variables variables del proceso
     * @param errores   lista para acumular errores
     */
    private void validarTelefonoContacto(Map<String, Object> variables, List<String> errores) {
        if (!variables.containsKey("telefonoContacto")) {
            errores.add("El campo 'telefonoContacto' es obligatorio");
            return;
        }

        Object telefonoObj = variables.get("telefonoContacto");

        if (telefonoObj == null) {
            errores.add("El 'telefonoContacto' no puede ser nulo");
            return;
        }

        String telefono = telefonoObj.toString().trim();

        if (StringUtils.isBlank(telefono)) {
            errores.add("El 'telefonoContacto' no puede estar vacío");
            return;
        }

        // Eliminar espacios y guiones para validación
        String telefonoLimpio = telefono.replaceAll("[\\s-]", "");

        if (!PATRON_TELEFONO.matcher(telefonoLimpio).matches()) {
            errores.add("El 'telefonoContacto' tiene un formato inválido. Use formato internacional (+34600123456)");
        }
    }
}
