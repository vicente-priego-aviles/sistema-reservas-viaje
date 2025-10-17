package dev.javacadabra.reservasviaje.cliente.aplicacion.servicio;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ClienteEntradaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteSalidaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.mapper.ClienteMapper;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.salida.ClienteRepositorioPuertoSalida;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.valorobjeto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServicioAplicacion implements
        ObtenerClientePuertoEntrada,
        CrearClientePuertoEntrada,
        ValidarTarjetaPuertoEntrada,
        ActualizarEstadoPuertoEntrada {

    private final ClienteRepositorioPuertoSalida repositorio;
    private final ClienteMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ClienteSalidaDTO obtenerCliente(String clienteId) {
        log.info("🔍 Obteniendo cliente: {}", clienteId);

        Cliente cliente = repositorio.buscarPorId(new ClienteId(clienteId))
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));

        log.info("✅ Cliente encontrado: {}", cliente.getNombre().valor());
        return mapper.toDTO(cliente);
    }

    @Override
    @Transactional
    public ClienteSalidaDTO crearCliente(ClienteEntradaDTO dto) {
        log.info("🔍 Creando nuevo cliente: {}", dto.nombre());

        Cliente cliente = mapper.toDomain(dto);
        Cliente guardado = repositorio.guardar(cliente);

        log.info("✅ Cliente creado: {}", guardado.getId().valor());
        return mapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public void validarTarjeta(String clienteId) {
        log.info("🔍 Validando tarjeta del cliente: {}", clienteId);

        Cliente cliente = repositorio.buscarPorId(new ClienteId(clienteId))
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));

        cliente.validarTarjeta();

        log.info("✅ Tarjeta validada correctamente");
    }

    @Override
    @Transactional
    public void actualizarEstado(String clienteId, EstadoCliente nuevoEstado) {
        log.info("🔍 Actualizando estado del cliente {} a: {}", clienteId, nuevoEstado);

        Cliente cliente = repositorio.buscarPorId(new ClienteId(clienteId))
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));

        // Lógica de cambio de estado según el nuevo estado
        switch (nuevoEstado) {
            case EN_PROCESO_RESERVA -> cliente.iniciarProcesoReserva();
            case ACTIVO -> cliente.revertirEstado();
            // más casos...
        }

        repositorio.guardar(cliente);
        log.info("✅ Estado actualizado correctamente");
    }
}