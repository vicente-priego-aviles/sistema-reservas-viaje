package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.servicio.ClienteServicio;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteBloqueadoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteInactivoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.TarjetaNoEncontradaExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.entidad.TarjetaCredito;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.CVV;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.ClienteId;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.NumeroTarjeta;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.TarjetaId;
import dev.javacadabra.reservasviaje.cliente.dominio.repositorio.ClienteRepositorio;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker de Camunda que actualiza la información de una tarjeta de crédito.
 *
 * <p>Este worker permite actualizar de forma flexible:
 * <ul>
 *   <li>Solo la fecha de expiración</li>
 *   <li>Solo el número de tarjeta + CVV</li>
 *   <li>Ambos datos simultáneamente</li>
 * </ul>
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>{@code clienteId} (String) - ID del cliente propietario (UUID)</li>
 *   <li>{@code tarjetaId} (String) - ID de la tarjeta a actualizar (UUID)</li>
 *   <li>{@code nuevoNumeroTarjeta} (String, opcional) - Nuevo número de tarjeta</li>
 *   <li>{@code nuevoCvv} (String, opcional) - Nuevo CVV (obligatorio si hay nuevoNumeroTarjeta)</li>
 *   <li>{@code nuevaFechaExpiracion} (String, opcional) - Nueva fecha MM/YYYY</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> Al menos uno de los campos opcionales debe estar presente.
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>{@code tarjetaActualizada} (Boolean) - true si se actualizó correctamente</li>
 *   <li>{@code numeroActualizado} (Boolean) - true si se actualizó el número</li>
 *   <li>{@code fechaActualizada} (Boolean) - true si se actualizó la fecha</li>
 *   <li>{@code tipoTarjeta} (String) - Tipo de tarjeta (VISA, MASTERCARD, etc.)</li>
 *   <li>{@code numeroEnmascarado} (String) - Número enmascarado (**** **** **** 1234)</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>{@code ERROR_CLIENTE_NO_ENCONTRADO} - Cliente no existe</li>
 *   <li>{@code ERROR_TARJETA_NO_ENCONTRADA} - Tarjeta no existe</li>
 *   <li>{@code ERROR_CLIENTE_BLOQUEADO} - Cliente está bloqueado</li>
 *   <li>{@code ERROR_CLIENTE_INACTIVO} - Cliente está inactivo</li>
 *   <li>{@code ERROR_DATOS_INVALIDOS} - Datos de entrada inválidos</li>
 *   <li>{@code ERROR_FECHA_PASADO} - Fecha de expiración en el pasado</li>
 *   <li>{@code ERROR_CVV_REQUERIDO} - CVV requerido al actualizar número</li>
 *   <li>{@code ERROR_SIN_CAMBIOS} - No se proporcionó ningún campo para actualizar</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActualizarInformacionTarjetaWorker {

    private final ClienteRepositorio clienteRepositorio;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("MM/yyyy");

    /**
     * Procesa la actualización de información de la tarjeta.
     *
     * @param job job activado por Camunda
     * @return mapa con variables de salida
     * @throws ZeebeBpmnError si ocurre algún error durante la actualización
     */
    @JobWorker(type = "actualizar-informacion-tarjeta", autoComplete = true)
    @Transactional
    public Map<String, Object> actualizarInformacionTarjeta(ActivatedJob job) {
        log.info("💳 Iniciando actualización de información de tarjeta - Job: {}", job.getKey());

        try {
            // 1. Extraer variables de entrada
            Map<String, Object> variables = job.getVariablesAsMap();

            String clienteId = extraerClienteId(variables);
            String tarjetaId = extraerTarjetaId(variables);

            log.info("🔍 Actualizando tarjeta {} del cliente {}", tarjetaId, clienteId);

            // 2. Obtener cliente y tarjeta
            Cliente cliente = obtenerCliente(clienteId);
            TarjetaCredito tarjeta = cliente.obtenerTarjeta(TarjetaId.de(tarjetaId));

            // 3. Extraer datos opcionales a actualizar
            String nuevoNumeroStr = extraerCampoOpcional(variables, "nuevoNumeroTarjeta");
            String nuevoCvvStr = extraerCampoOpcional(variables, "nuevoCvv");
            String nuevaFechaStr = extraerCampoOpcional(variables, "nuevaFechaExpiracion");

            // 4. Validar que al menos un campo esté presente
            if (StringUtils.isAllBlank(nuevoNumeroStr, nuevoCvvStr, nuevaFechaStr)) {
                throw new IllegalArgumentException(
                        "Debe proporcionar al menos un campo para actualizar: " +
                        "nuevoNumeroTarjeta, nuevoCvv o nuevaFechaExpiracion"
                );
            }

            // 5. Actualizar información según campos proporcionados
            boolean numeroActualizado = false;
            boolean fechaActualizada = false;

            // Actualizar número + CVV si están presentes
            if (StringUtils.isNotBlank(nuevoNumeroStr)) {
                if (StringUtils.isBlank(nuevoCvvStr)) {
                    throw new IllegalArgumentException(
                            "El CVV es obligatorio cuando se actualiza el número de tarjeta"
                    );
                }

                actualizarNumeroYCvv(tarjeta, nuevoNumeroStr, nuevoCvvStr);
                numeroActualizado = true;
                log.info("✅ Número de tarjeta actualizado");
            }

            // Actualizar fecha de expiración si está presente
            if (StringUtils.isNotBlank(nuevaFechaStr)) {
                actualizarFechaExpiracion(tarjeta, nuevaFechaStr);
                fechaActualizada = true;
                log.info("✅ Fecha de expiración actualizada");
            }

            // 6. Guardar cambios
            clienteRepositorio.save(cliente);

            log.info("✅ Información de tarjeta actualizada correctamente: {} - Tipo: {}",
                    tarjetaId, tarjeta.getTipoTarjeta());

            // 7. Retornar variables de salida
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("tarjetaActualizada", true);
            resultado.put("numeroActualizado", numeroActualizado);
            resultado.put("fechaActualizada", fechaActualizada);
            resultado.put("tipoTarjeta", tarjeta.getTipoTarjeta().name());
            resultado.put("numeroEnmascarado", tarjeta.obtenerNumeroEnmascarado());

            return resultado;

        } catch (ClienteNoEncontradoExcepcion e) {
            log.error("❌ Cliente no encontrado: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_NO_ENCONTRADO",
                    "El cliente especificado no existe: " + e.getMessage(),
                    Map.of("tarjetaActualizada", false)
            );

        } catch (TarjetaNoEncontradaExcepcion e) {
            log.error("❌ Tarjeta no encontrada: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_TARJETA_NO_ENCONTRADA",
                    "La tarjeta especificada no existe: " + e.getMessage(),
                    Map.of("tarjetaActualizada", false)
            );

        } catch (ClienteBloqueadoExcepcion e) {
            log.error("❌ Cliente bloqueado: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_BLOQUEADO",
                    "No se puede actualizar la tarjeta de un cliente bloqueado: " + e.getMessage(),
                    Map.of("tarjetaActualizada", false)
            );

        } catch (ClienteInactivoExcepcion e) {
            log.error("❌ Cliente inactivo: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_CLIENTE_INACTIVO",
                    "No se puede actualizar la tarjeta de un cliente inactivo: " + e.getMessage(),
                    Map.of("tarjetaActualizada", false)
            );

        } catch (IllegalArgumentException e) {
            log.error("❌ Datos inválidos: {}", e.getMessage());

            // Determinar código de error específico
            String codigoError = determinarCodigoError(e.getMessage());

            throw new ZeebeBpmnError(
                    codigoError,
                    "Error en los datos de entrada: " + e.getMessage(),
                    Map.of("tarjetaActualizada", false)
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al actualizar información de tarjeta: {}", e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_ACTUALIZAR_TARJETA",
                    "Error inesperado al actualizar tarjeta: " + e.getMessage(),
                    Map.of("tarjetaActualizada", false)
            );
        }
    }

    /**
     * Extrae el ID del cliente de las variables del proceso.
     *
     * @param variables variables del proceso
     * @return ID del cliente (UUID)
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
     * Extrae el ID de la tarjeta de las variables del proceso.
     *
     * @param variables variables del proceso
     * @return ID de la tarjeta (UUID)
     * @throws IllegalArgumentException si el tarjetaId no está presente o es inválido
     */
    private String extraerTarjetaId(Map<String, Object> variables) {
        if (!variables.containsKey("tarjetaId")) {
            throw new IllegalArgumentException("La variable 'tarjetaId' es obligatoria");
        }

        Object tarjetaIdObj = variables.get("tarjetaId");

        if (tarjetaIdObj == null) {
            throw new IllegalArgumentException("El 'tarjetaId' no puede ser nulo");
        }

        String tarjetaId = tarjetaIdObj.toString().trim();

        if (StringUtils.isBlank(tarjetaId)) {
            throw new IllegalArgumentException("El 'tarjetaId' no puede estar vacío");
        }

        return tarjetaId;
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
     * Obtiene el cliente del repositorio.
     *
     * @param clienteId ID del cliente
     * @return cliente encontrado
     * @throws ClienteNoEncontradoExcepcion si el cliente no existe
     */
    private Cliente obtenerCliente(String clienteId) {
        return clienteRepositorio.findById(ClienteId.de(clienteId))
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));
    }

    /**
     * Actualiza el número de tarjeta y CVV.
     *
     * @param tarjeta tarjeta a actualizar
     * @param nuevoNumeroStr nuevo número de tarjeta
     * @param nuevoCvvStr nuevo CVV
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void actualizarNumeroYCvv(TarjetaCredito tarjeta, String nuevoNumeroStr, String nuevoCvvStr) {
        try {
            // Crear value objects
            NumeroTarjeta nuevoNumero = NumeroTarjeta.de(nuevoNumeroStr);
            CVV nuevoCvv = CVV.crear(nuevoCvvStr);

            // Actualizar en la entidad
            tarjeta.actualizarNumeroTarjeta(nuevoNumero, nuevoCvv);

            log.debug("🔍 Número de tarjeta actualizado - Tipo: {}", tarjeta.getTipoTarjeta());

        } catch (IllegalArgumentException e) {
            log.error("❌ Error al actualizar número de tarjeta: {}", e.getMessage());
            throw new IllegalArgumentException(
                    "Error al actualizar número de tarjeta: " + e.getMessage(), e
            );
        }
    }

    /**
     * Actualiza la fecha de expiración de la tarjeta.
     *
     * @param tarjeta tarjeta a actualizar
     * @param nuevaFechaStr nueva fecha en formato MM/YYYY
     * @throws IllegalArgumentException si la fecha es inválida
     */
    private void actualizarFechaExpiracion(TarjetaCredito tarjeta, String nuevaFechaStr) {
        try {
            // Parsear fecha MM/YYYY
            YearMonth nuevaFecha = YearMonth.parse(nuevaFechaStr, FORMATO_FECHA);

            // Validar que no esté en el pasado
            if (nuevaFecha.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException(
                        "La fecha de expiración no puede estar en el pasado: " + nuevaFechaStr
                );
            }

            // Actualizar en la entidad
            tarjeta.actualizarFechaExpiracion(nuevaFecha);

            log.debug("🔍 Fecha de expiración actualizada: {}", nuevaFecha);

        } catch (DateTimeParseException e) {
            log.error("❌ Error al parsear fecha: {}", e.getMessage());
            throw new IllegalArgumentException(
                    "Formato de fecha inválido. Use MM/YYYY (ejemplo: 12/2027): " + nuevaFechaStr, e
            );
        } catch (IllegalArgumentException e) {
            log.error("❌ Error al actualizar fecha de expiración: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Determina el código de error BPMN según el mensaje de excepción.
     *
     * @param mensajeError mensaje de error
     * @return código de error BPMN
     */
    private String determinarCodigoError(String mensajeError) {
        String mensajeLower = mensajeError.toLowerCase();

        if (mensajeLower.contains("cvv") && mensajeLower.contains("obligatorio")) {
            return "ERROR_CVV_REQUERIDO";
        }

        if (mensajeLower.contains("pasado") || mensajeLower.contains("expirada")) {
            return "ERROR_FECHA_PASADO";
        }

        if (mensajeLower.contains("al menos un campo")) {
            return "ERROR_SIN_CAMBIOS";
        }

        return "ERROR_DATOS_INVALIDOS";
    }
}