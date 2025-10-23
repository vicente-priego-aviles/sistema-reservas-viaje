package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearTarjetaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.CrearClienteUseCase;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.DniDuplicadoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.EmailDuplicadoExcepcion;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker de Camunda para crear un nuevo cliente.
 *
 * <p>Este worker escucha jobs de tipo "crear-cliente" y ejecuta el caso
 * de uso de creación de clientes en el sistema.
 *
 * <p><strong>Job Type:</strong> {@code crear-cliente}
 *
 * <p><strong>Variables de entrada esperadas:</strong>
 * <ul>
 *   <li>dni (String): DNI del cliente</li>
 *   <li>nombre (String): Nombre del cliente</li>
 *   <li>apellidos (String): Apellidos del cliente</li>
 *   <li>email (String): Email del cliente</li>
 *   <li>telefono (String, opcional): Teléfono del cliente</li>
 *   <li>fechaNacimiento (String): Fecha de nacimiento formato ISO (yyyy-MM-dd)</li>
 *   <li>calle (String): Dirección - calle</li>
 *   <li>ciudad (String): Dirección - ciudad</li>
 *   <li>codigoPostal (String): Dirección - código postal</li>
 *   <li>provincia (String): Dirección - provincia</li>
 *   <li>pais (String): Dirección - país</li>
 *   <li>numeroTarjeta (String): Número de tarjeta de crédito</li>
 *   <li>fechaExpiracion (String): Fecha de expiración (MM/YY)</li>
 *   <li>cvv (String): CVV de la tarjeta</li>
 * </ul>
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>clienteId (String): ID del cliente creado</li>
 *   <li>clienteCreado (Boolean): true si se creó exitosamente</li>
 *   <li>nombreCompleto (String): Nombre completo del cliente</li>
 *   <li>estadoCliente (String): Estado inicial del cliente</li>
 * </ul>
 *
 * <p><strong>Errores BPMN que puede lanzar:</strong>
 * <ul>
 *   <li>ERROR_EMAIL_DUPLICADO: El email ya existe en el sistema</li>
 *   <li>ERROR_DNI_DUPLICADO: El DNI ya existe en el sistema</li>
 *   <li>ERROR_VALIDACION: Datos de entrada inválidos</li>
 *   <li>ERROR_CREACION_CLIENTE: Error inesperado al crear el cliente</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CrearClienteWorker {

    private final CrearClienteUseCase crearClienteUseCase;

    /**
     * Maneja el job de crear cliente desde Camunda.
     *
     * @param job job activado por Zeebe
     * @return variables de salida para el proceso BPMN
     */
    @JobWorker(type = "crear-cliente", autoComplete = true)
    public Map<String, Object> manejarCrearCliente(ActivatedJob job) {
        log.info("🚀 Procesando job crear-cliente: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        log.debug("📥 Variables recibidas: {}", variables);

        try {
            // Validar variables obligatorias
            validarVariablesObligatorias(variables);

            // Construir DTO de creación
            CrearClienteDTO dto = construirCrearClienteDTO(variables);

            // Ejecutar caso de uso
            log.info("📝 Creando cliente con email: {}", dto.email());
            ClienteDTO clienteCreado = crearClienteUseCase.crear(dto);

            // Preparar variables de salida
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("clienteId", clienteCreado.clienteId());
            resultado.put("clienteCreado", true);
            resultado.put("nombreCompleto", clienteCreado.datosPersonales().nombreCompleto());
            resultado.put("estadoCliente", clienteCreado.estado());
            resultado.put("cantidadTarjetas", clienteCreado.cantidadTarjetas());

            log.info("✅ Cliente creado exitosamente: {} - {}",
                    clienteCreado.clienteId(),
                    clienteCreado.datosPersonales().nombreCompleto());

            return resultado;

        } catch (EmailDuplicadoExcepcion e) {
            log.error("❌ Error: Email duplicado - {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_EMAIL_DUPLICADO",
                    "El email ya existe en el sistema: " + e.getMessage(),
                    Map.of(
                            "emailDuplicado", e.getMessage(),
                            "errorType", "EMAIL_DUPLICADO"
                    )
            );

        } catch (DniDuplicadoExcepcion e) {
            log.error("❌ Error: DNI duplicado - {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_DNI_DUPLICADO",
                    "El DNI ya existe en el sistema",
                    Map.of(
                            "errorType", "DNI_DUPLICADO"
                    )
            );

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            throw new ZeebeBpmnError(
                    "ERROR_VALIDACION",
                    "Datos de entrada inválidos: " + e.getMessage(),
                    Map.of(
                            "errorType", "VALIDACION",
                            "mensajeError", e.getMessage()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Error inesperado al crear cliente: {}", e.getMessage(), e);
            throw new ZeebeBpmnError(
                    "ERROR_CREACION_CLIENTE",
                    "Error inesperado: " + e.getMessage(),
                    Map.of(
                            "errorType", "ERROR_INESPERADO",
                            "mensajeError", e.getMessage()
                    )
            );
        }
    }

    /**
     * Valida que todas las variables obligatorias estén presentes.
     *
     * @param variables variables del job
     * @throws IllegalArgumentException si falta alguna variable obligatoria
     */
    private void validarVariablesObligatorias(Map<String, Object> variables) {
        String[] camposObligatorios = {
                "dni", "nombre", "apellidos", "email", "fechaNacimiento",
                "calle", "ciudad", "codigoPostal", "provincia", "pais",
                "numeroTarjeta", "fechaExpiracion", "cvv"
        };

        for (String campo : camposObligatorios) {
            if (!variables.containsKey(campo) || variables.get(campo) == null) {
                throw new IllegalArgumentException("Campo obligatorio faltante: " + campo);
            }
        }
    }

    /**
     * Construye el DTO de creación de cliente desde las variables del job.
     *
     * @param variables variables del job
     * @return DTO de creación de cliente
     */
    private CrearClienteDTO construirCrearClienteDTO(Map<String, Object> variables) {
        // Construir DTO de tarjeta
        CrearTarjetaDTO tarjetaDTO = new CrearTarjetaDTO(
                variables.get("numeroTarjeta").toString(),
                variables.get("fechaExpiracion").toString(),
                variables.get("cvv").toString()
        );

        // Parsear fecha de nacimiento
        LocalDate fechaNacimiento = LocalDate.parse(variables.get("fechaNacimiento").toString());

        // Construir DTO de cliente
        return new CrearClienteDTO(
                variables.get("dni").toString(),
                variables.get("nombre").toString(),
                variables.get("apellidos").toString(),
                variables.get("email").toString(),
                variables.containsKey("telefono") ? variables.get("telefono").toString() : null,
                fechaNacimiento,
                variables.get("calle").toString(),
                variables.get("ciudad").toString(),
                variables.get("codigoPostal").toString(),
                variables.get("provincia").toString(),
                variables.get("pais").toString(),
                tarjetaDTO
        );
    }
}