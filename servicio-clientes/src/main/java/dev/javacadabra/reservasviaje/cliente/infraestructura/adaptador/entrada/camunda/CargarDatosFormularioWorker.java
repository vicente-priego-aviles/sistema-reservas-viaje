package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.ConsultarClienteUseCase;
import io.camunda.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Worker de Camunda que precarga los datos necesarios para el formulario de inicio de reserva.
 *
 * <p>Se ejecuta antes de mostrar el user task {@code iniciar-solicitud-reserva}, de modo que
 * el formulario Camunda pueda usar {@code valuesExpression = "=clientesDisponibles"} en el
 * campo de selección de cliente.
 *
 * <p><strong>Variables de salida:</strong>
 * <ul>
 *   <li>{@code clientesDisponibles} — lista de objetos {@code {label, value}} con los
 *       clientes en estado ACTIVO, lista para usar en un componente {@code select} de
 *       Camunda Forms.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CargarDatosFormularioWorker {

    private final ConsultarClienteUseCase consultarClienteUseCase;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @JobWorker(type = "cargar-datos-formulario", autoComplete = true)
    public Map<String, Object> cargarDatos() {
        log.info("📋 Cargando clientes ACTIVO para formulario de inicio");

        List<Map<String, String>> clientesDisponibles = consultarClienteUseCase.listarTodos()
                .stream()
                .filter(c -> "ACTIVO".equals(c.estado()))
                .map(c -> Map.of(
                        "label", c.nombreCompleto(),
                        "value", c.clienteId()
                ))
                .toList();

        log.info("✅ {} clientes disponibles cargados", clientesDisponibles.size());

        String manana = LocalDate.now().plusDays(1).format(FORMATO_FECHA);

        Map<String, Object> variables = new HashMap<>();
        variables.put("clientesDisponibles", clientesDisponibles);
        variables.put("fechaInicio", manana);
        variables.put("fechaFin", manana);
        return variables;
    }
}
