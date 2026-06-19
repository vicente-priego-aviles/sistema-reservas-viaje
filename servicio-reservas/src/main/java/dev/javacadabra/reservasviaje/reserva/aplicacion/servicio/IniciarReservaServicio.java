package dev.javacadabra.reservasviaje.reserva.aplicacion.servicio;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.IniciarReservaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.IniciarReservaRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.IniciarReservaCasoUso;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IniciarReservaServicio implements IniciarReservaCasoUso {

    private final ZeebeClient zeebeClient;

    @Override
    public IniciarReservaRespuestaDTO ejecutar(IniciarReservaDTO dto) {
        log.info("Iniciando proceso de reserva para cliente: {}", dto.getClienteId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("clienteId", dto.getClienteId());
        variables.put("destino", dto.getDestino());
        variables.put("fechaInicio", dto.getFechaInicio());
        variables.put("fechaFin", dto.getFechaFin());
        variables.put("numeroPasajeros", dto.getNumeroPasajeros());
        variables.put("emailContacto", dto.getEmailContacto());
        variables.put("telefonoContacto", dto.getTelefonoContacto());
        if (dto.getOrigen() != null) {
            variables.put("origen", dto.getOrigen());
        }

        ProcessInstanceEvent instance = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("proceso-principal")
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        log.info("Proceso iniciado con key: {}", instance.getProcessInstanceKey());

        return IniciarReservaRespuestaDTO.builder()
                .processInstanceKey(instance.getProcessInstanceKey())
                .estado("INICIADA")
                .mensaje("Reserva iniciada correctamente")
                .build();
    }
}
