package dev.javacadabra.reservasviaje.reserva.aplicacion.servicio;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.IniciarReservaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.ReservaIniciadaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.IniciarReservaPuertoEntrada;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida.ReservaRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.agregado.ReservaViaje;
import dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor.*;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaServicioAplicacion implements IniciarReservaPuertoEntrada {

    private final ZeebeClient zeebeClient;
    private final ReservaRepositorioPuertoSalida repositorio;

    @Override
    @Transactional
    public ReservaIniciadaDTO iniciarReserva(IniciarReservaDTO dto) {
        log.info("🚀 Iniciando reserva de viaje: {} -> {}", dto.origen(), dto.destino());

        // 1. Crear el agregado de dominio
        ReservaId reservaId = ReservaId.generar();

        ReservaViaje reserva = ReservaViaje.builder()
                .id(reservaId)
                .clienteId(dto.clienteId())
                .origen(dto.origen())
                .destino(dto.destino())
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .monto(dto.monto())
                .estado(EstadoReserva.INICIADA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // 2. Guardar en BD antes de iniciar el proceso
        reserva = repositorio.guardar(reserva);
        log.info("💾 Reserva guardada con ID: {}", reservaId.valor());

        // 3. Preparar variables para Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("reservaId", reservaId.valor());
        variables.put("clienteId", dto.clienteId());
        variables.put("origen", dto.origen());
        variables.put("destino", dto.destino());
        variables.put("fechaInicio", dto.fechaInicio().toString());
        variables.put("fechaFin", dto.fechaFin().toString());
        variables.put("monto", dto.monto());

        // 4. Iniciar el proceso BPMN en Camunda
        log.info("📋 Iniciando proceso BPMN: proceso-principal");

        ProcessInstanceEvent processInstance = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("proceso-principal")
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        log.info("✅ Proceso iniciado - Instance Key: {}", processInstance.getProcessInstanceKey());

        // 5. Actualizar el agregado con la clave del proceso
        reserva.iniciarProceso(processInstance.getProcessInstanceKey());
        repositorio.guardar(reserva);

        // 6. Retornar DTO de respuesta
        return new ReservaIniciadaDTO(
                reservaId.valor(),
                processInstance.getProcessInstanceKey(),
                EstadoReserva.INICIADA.name(),
                LocalDateTime.now(),
                "Reserva iniciada correctamente. El proceso BPMN está en ejecución."
        );
    }
}
