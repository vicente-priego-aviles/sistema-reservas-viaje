package dev.javacadabra.reservasviaje.cliente.aplicacion.servicio;

import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDatosPersonalesDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.ActualizarDireccionDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.AgregarTarjetaDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.ClienteResumenDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.dto.salida.TarjetaCreditoDTO;
import dev.javacadabra.reservasviaje.cliente.aplicacion.mapper.ClienteMapper;
import dev.javacadabra.reservasviaje.cliente.aplicacion.mapper.TarjetaCreditoMapper;
import dev.javacadabra.reservasviaje.cliente.aplicacion.puerto.entrada.*;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.ClienteNoEncontradoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.DniDuplicadoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.EmailDuplicadoExcepcion;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado.Cliente;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.entidad.TarjetaCredito;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import dev.javacadabra.reservasviaje.cliente.dominio.repositorio.ClienteRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

/**
 * Servicio de aplicación que implementa todos los casos de uso de Cliente.
 *
 * <p>Este servicio actúa como orquestador entre la capa de dominio y la
 * infraestructura, implementando todos los use cases definidos en los
 * puertos de entrada.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Orquestar operaciones entre dominio e infraestructura</li>
 *   <li>Validar reglas de negocio transversales (unicidad email/DNI)</li>
 *   <li>Convertir entre DTOs y entidades de dominio (usando mappers)</li>
 *   <li>Gestionar transacciones (@Transactional)</li>
 *   <li>Manejar encriptación de datos sensibles (placeholder por ahora)</li>
 *   <li>Propagar eventos de dominio</li>
 * </ul>
 *
 * <p><strong>Transaccionalidad:</strong>
 * Todos los métodos que modifican estado son transaccionales. Los eventos
 * de dominio se publican automáticamente al finalizar la transacción.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ClienteServicio implements
        CrearClienteUseCase,
        ActualizarClienteUseCase,
        ConsultarClienteUseCase,
        GestionarTarjetasUseCase,
        GestionarEstadoClienteUseCase {

    private final ClienteRepositorio clienteRepositorio;
    private final ClienteMapper clienteMapper;
    private final TarjetaCreditoMapper tarjetaCreditoMapper;

    // ==================== CREAR CLIENTE ====================

    @Override
    @Transactional
    public ClienteDTO crear(CrearClienteDTO dto) {
        log.info("🚀 Iniciando creación de cliente con email: {}", dto.obtenerEmailNormalizado());

        // Validar unicidad de email
        validarEmailUnico(dto.obtenerEmailNormalizado(), null);

        // Validar unicidad de DNI
        validarDniUnico(dto.obtenerDniNormalizado());

        // Crear value objects de dominio usando el mapper
        DatosPersonales datosPersonales = clienteMapper.toDatosPersonales(dto);
        Direccion direccion = clienteMapper.toDireccion(dto);

        // Crear tarjeta inicial (encriptar datos sensibles - placeholder por ahora)
        TarjetaCredito tarjetaInicial = crearTarjetaDesdeDTO(
                ClienteId.generar(), // Temporal, se reemplazará por el ID real
                dto.tarjeta()
        );

        // Crear agregado Cliente
        Cliente cliente = Cliente.crear(datosPersonales, direccion, tarjetaInicial);

        // Persistir cliente (eventos se publican automáticamente)
        Cliente clienteGuardado = clienteRepositorio.save(cliente);

        log.info("✅ Cliente creado exitosamente: {}", clienteGuardado.getClienteId());

        return clienteMapper.aDTO(clienteGuardado);
    }

    // ==================== ACTUALIZAR CLIENTE ====================

    @Override
    @Transactional
    public ClienteDTO actualizarDatosPersonales(String clienteId, ActualizarDatosPersonalesDTO dto) {
        log.info("🔄 Actualizando datos personales del cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        // Normalizar email del DTO
        String emailNormalizado = dto.email().trim().toLowerCase();

        // Validar unicidad de email si cambió (usar el accessor del record)
        if (!cliente.getDatosPersonales().getEmail().equals(emailNormalizado)) {
            validarEmailUnico(emailNormalizado, clienteId);
        }

        // Crear nuevos datos personales usando el mapper, pasando los datos actuales
        // para preservar DNI y fecha de nacimiento (campos inmutables)
        DatosPersonales nuevosDatosPersonales = clienteMapper.toDatosPersonales(
                dto,
                cliente.getDatosPersonales() // Pasar datos actuales para preservar inmutables
        );

        // Actualizar en el agregado
        cliente.actualizarDatosPersonales(nuevosDatosPersonales);

        // Persistir cambios
        Cliente clienteActualizado = clienteRepositorio.save(cliente);

        log.info("✅ Datos personales actualizados para cliente: {}", clienteId);

        return clienteMapper.aDTO(clienteActualizado);
    }

    @Override
    @Transactional
    public ClienteDTO actualizarDireccion(String clienteId, ActualizarDireccionDTO dto) {
        log.info("🔄 Actualizando dirección del cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        // Crear nueva dirección usando el mapper
        Direccion nuevaDireccion = clienteMapper.aDireccion(dto);

        // Actualizar en el agregado
        cliente.actualizarDireccion(nuevaDireccion);

        // Persistir cambios
        Cliente clienteActualizado = clienteRepositorio.save(cliente);

        log.info("✅ Dirección actualizada para cliente: {}", clienteId);

        return clienteMapper.aDTO(clienteActualizado);
    }

    // ==================== CONSULTAR CLIENTE ====================

    @Override
    public ClienteDTO buscarPorId(String clienteId) {
        log.debug("🔍 Buscando cliente por ID: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        return clienteMapper.aDTO(cliente);
    }

    @Override
    public ClienteDTO buscarPorEmail(String email) {
        log.debug("🔍 Buscando cliente por email: {}", email);

        String emailNormalizado = email.trim().toLowerCase();
        Cliente cliente = clienteRepositorio.findByEmail(emailNormalizado)
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion("email", emailNormalizado));

        return clienteMapper.aDTO(cliente);
    }

    @Override
    public ClienteDTO buscarPorDni(String dni) {
        log.debug("🔍 Buscando cliente por DNI");

        String dniNormalizado = dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        Cliente cliente = clienteRepositorio.findByDni(dniNormalizado)
                .orElseThrow(() -> {
                    // Enmascarar DNI en el mensaje de error por seguridad
                    String dniEnmascarado = dniNormalizado.substring(0, 3) + "****" +
                                            dniNormalizado.substring(dniNormalizado.length() - 2);
                    return new ClienteNoEncontradoExcepcion("DNI", dniEnmascarado);
                });

        return clienteMapper.aDTO(cliente);
    }

    @Override
    public List<ClienteResumenDTO> listarTodos() {
        log.debug("🔍 Listando todos los clientes");

        List<Cliente> clientes = clienteRepositorio.findAll();

        log.debug("📊 Se encontraron {} clientes", clientes.size());

        return clienteMapper.toResumenDTOList(clientes);
    }

    @Override
    public boolean existePorEmail(String email) {
        String emailNormalizado = email.trim().toLowerCase();
        return clienteRepositorio.existsByEmail(emailNormalizado);
    }

    @Override
    public boolean existePorDni(String dni) {
        String dniNormalizado = dni.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        return clienteRepositorio.existsByDni(dniNormalizado);
    }

    @Override
    public long contarClientes() {
        return clienteRepositorio.count();
    }

    // ==================== GESTIONAR TARJETAS ====================

    @Override
    @Transactional
    public ClienteDTO agregarTarjeta(String clienteId, AgregarTarjetaDTO dto) {
        log.info("💳 Agregando tarjeta al cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        // Crear tarjeta (encriptar datos sensibles - placeholder por ahora)
        TarjetaCredito nuevaTarjeta = crearTarjetaDesdeDTO(
                ClienteId.de(clienteId),
                dto.tarjeta()
        );

        // Agregar al agregado
        cliente.agregarTarjeta(nuevaTarjeta);

        // Persistir cambios
        Cliente clienteActualizado = clienteRepositorio.save(cliente);

        log.info("✅ Tarjeta agregada al cliente: {} - Total tarjetas: {}",
                clienteId, clienteActualizado.getCantidadTarjetas());

        return clienteMapper.aDTO(clienteActualizado);
    }

    @Override
    @Transactional
    public ClienteDTO eliminarTarjeta(String clienteId, String tarjetaId, String motivo) {
        log.info("🗑️ Eliminando tarjeta {} del cliente: {}", tarjetaId, clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        // Eliminar del agregado
        cliente.eliminarTarjeta(TarjetaId.de(tarjetaId), motivo);

        // Persistir cambios
        Cliente clienteActualizado = clienteRepositorio.save(cliente);

        log.info("✅ Tarjeta eliminada del cliente: {} - Tarjetas restantes: {}",
                clienteId, clienteActualizado.getCantidadTarjetas());

        return clienteMapper.aDTO(clienteActualizado);
    }

    @Override
    public List<TarjetaCreditoDTO> obtenerTarjetasCliente(String clienteId) {
        log.debug("🔍 Obteniendo tarjetas del cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        return tarjetaCreditoMapper.aDTOList(cliente.obtenerTarjetas());
    }

    @Override
    public List<TarjetaCreditoDTO> obtenerTarjetasValidasCliente(String clienteId) {
        log.debug("🔍 Obteniendo tarjetas válidas del cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        return tarjetaCreditoMapper.aDTOList(cliente.obtenerTarjetasValidas());
    }

    @Override
    public TarjetaCreditoDTO obtenerTarjeta(String clienteId, String tarjetaId) {
        log.debug("🔍 Obteniendo tarjeta {} del cliente: {}", tarjetaId, clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);
        TarjetaCredito tarjeta = cliente.obtenerTarjeta(TarjetaId.de(tarjetaId));

        return tarjetaCreditoMapper.aDTO(tarjeta);
    }

    // ==================== GESTIONAR ESTADO ====================

    @Override
    @Transactional
    public ClienteDTO activarCliente(String clienteId) {
        log.info("✅ Activando cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.activar();

        Cliente clienteActivado = clienteRepositorio.save(cliente);

        log.info("✅ Cliente activado exitosamente: {}", clienteId);

        return clienteMapper.aDTO(clienteActivado);
    }

    @Override
    @Transactional
    public ClienteDTO bloquearCliente(String clienteId, String motivo, boolean requiereRevisionManual) {
        log.warn("⚠️ Bloqueando cliente: {} - Motivo: {}", clienteId, motivo);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.bloquear(motivo, requiereRevisionManual);

        Cliente clienteBloqueado = clienteRepositorio.save(cliente);

        log.warn("🔒 Cliente bloqueado: {}", clienteId);

        return clienteMapper.aDTO(clienteBloqueado);
    }

    @Override
    @Transactional
    public ClienteDTO desbloquearCliente(String clienteId, String motivoDesbloqueo, String administrador) {
        log.info("🔓 Desbloqueando cliente: {} por {}", clienteId, administrador);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.desbloquear(motivoDesbloqueo, administrador);

        Cliente clienteDesbloqueado = clienteRepositorio.save(cliente);

        log.info("✅ Cliente desbloqueado exitosamente: {}", clienteId);

        return clienteMapper.aDTO(clienteDesbloqueado);
    }

    @Override
    @Transactional
    public ClienteDTO iniciarProcesoReserva(String clienteId) {
        log.info("🚀 Iniciando proceso de reserva para cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.iniciarProcesoReserva(clienteId);

        Cliente clienteActualizado = clienteRepositorio.save(cliente);

        log.info("✅ Proceso de reserva iniciado para cliente: {}", clienteId);

        return clienteMapper.aDTO(clienteActualizado);
    }

    /**
     * Confirma la reserva de un cliente (sin reservaId específico).
     *
     * <p><strong>NOTA:</strong> Este método es parte de la interfaz {@link GestionarEstadoClienteUseCase}
     * y no recibe un reservaId específico. Para casos de uso de Camunda donde se necesita
     * el reservaId, usar {@link #confirmarReservaConId(String, String)}.
     *
     * @param clienteId ID del cliente (UUID)
     * @return cliente actualizado como DTO
     * @throws ClienteNoEncontradoExcepcion si el cliente no existe
     * @throws IllegalStateException si el cliente no está en EN_PROCESO_RESERVA
     */
    @Override
    @Transactional
    public ClienteDTO confirmarReserva(String clienteId) {
        log.info("✅ Confirmando reserva para cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        // Llamar al método con reservaId = null (uso genérico)
        cliente.confirmarReserva(null);

        Cliente clienteActualizado = clienteRepositorio.save(cliente);

        log.info("✅ Reserva confirmada para cliente: {}", clienteId);

        return clienteMapper.aDTO(clienteActualizado);
    }

    /**
     * Finaliza el proceso de reserva de un cliente (sin reservaId específico).
     *
     * <p><strong>NOTA:</strong> Este método es parte de la interfaz {@link GestionarEstadoClienteUseCase}
     * y no recibe un reservaId específico. Para casos de uso de Camunda donde se necesita
     * el reservaId, usar {@link #finalizarReservaConId(String, String)}.
     *
     * @param clienteId ID del cliente (UUID)
     * @return cliente actualizado como DTO
     * @throws ClienteNoEncontradoExcepcion si el cliente no existe
     * @throws IllegalStateException si el cliente no está en RESERVA_CONFIRMADA
     */
    @Override
    @Transactional
    public ClienteDTO finalizarReserva(String clienteId) {
        log.info("🏁 Finalizando reserva para cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        // Llamar al método con reservaId = null (uso genérico)
        cliente.finalizarReserva(null);

        Cliente clienteActualizado = clienteRepositorio.save(cliente);

        log.info("✅ Reserva finalizada para cliente: {}", clienteId);

        return clienteMapper.aDTO(clienteActualizado);
    }

    /**
     * Obtiene el estado actual de un cliente.
     *
     * <p>Este método es utilizado por workers de Camunda para consultar
     * el estado del cliente antes de realizar transiciones.
     *
     * @param clienteId ID del cliente (UUID)
     * @return estado actual del cliente como String
     * @throws ClienteNoEncontradoExcepcion si el cliente no existe
     */
    public String obtenerEstadoCliente(String clienteId) {
        log.debug("🔍 Obteniendo estado del cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        String estado = cliente.getEstado().name();

        log.debug("📊 Estado del cliente {}: {}", clienteId, estado);

        return estado;
    }

    /**
     * Inicia el proceso de reserva para un cliente (con reservaId).
     *
     * <p>Este método es utilizado por el worker de Camunda para iniciar
     * un proceso de reserva con un ID de reserva específico.
     *
     * @param clienteId ID del cliente (UUID)
     * @param reservaId ID de la reserva
     * @throws ClienteNoEncontradoExcepcion si el cliente no existe
     * @throws IllegalStateException si el cliente no está en estado ACTIVO
     */
    @Transactional
    public void iniciarProcesoReservaConId(String clienteId, String reservaId) {
        log.info("🚀 Iniciando proceso de reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.iniciarProcesoReserva(reservaId);

        clienteRepositorio.save(cliente);

        log.info("✅ Proceso de reserva iniciado correctamente para cliente: {}", clienteId);
    }

    @Transactional
    public void confirmarReservaConId(String clienteId, String reservaId) {
        log.info("✅ Confirmando reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.confirmarReserva(reservaId);

        clienteRepositorio.save(cliente);

        log.info("✅ Reserva confirmada correctamente para cliente: {}", clienteId);
    }

    @Override
    @Transactional
    public ClienteDTO desactivarCliente(String clienteId) {
        log.info("⏸️ Desactivando cliente: {}", clienteId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.desactivar();

        Cliente clienteDesactivado = clienteRepositorio.save(cliente);

        log.info("✅ Cliente desactivado: {}", clienteId);

        return clienteMapper.aDTO(clienteDesactivado);
    }

    /**
     * Finaliza el proceso de reserva, devolviendo al cliente a estado ACTIVO (con reservaId).
     *
     * <p>Este método es utilizado por el worker de Camunda para finalizar
     * una reserva específica y retornar el cliente a estado ACTIVO.
     *
     * @param clienteId ID del cliente (UUID)
     * @param reservaId ID de la reserva
     * @throws ClienteNoEncontradoExcepcion si el cliente no existe
     * @throws IllegalStateException si el cliente no está en RESERVA_CONFIRMADA
     */
    @Transactional
    public void finalizarReservaConId(String clienteId, String reservaId) {
        log.info("🏁 Finalizando reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        Cliente cliente = buscarClientePorIdOLanzarExcepcion(clienteId);

        cliente.finalizarReserva(reservaId);

        clienteRepositorio.save(cliente);

        log.info("✅ Reserva finalizada correctamente para cliente: {}", clienteId);
    }

    // ==================== MÉTODOS PRIVADOS DE UTILIDAD ====================

    /**
     * Busca un cliente por ID o lanza excepción si no existe.
     *
     * @param clienteId identificador del cliente
     * @return cliente encontrado
     * @throws ClienteNoEncontradoExcepcion si no existe
     */
    private Cliente buscarClientePorIdOLanzarExcepcion(String clienteId) {
        return clienteRepositorio.findById(ClienteId.de(clienteId))
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));
    }

    /**
     * Valida que un email sea único en el sistema.
     *
     * @param email email a validar
     * @param clienteIdExcluir cliente a excluir de la validación (para updates)
     * @throws EmailDuplicadoExcepcion si el email ya existe
     */
    private void validarEmailUnico(String email, String clienteIdExcluir) {
        clienteRepositorio.findByEmail(email).ifPresent(clienteExistente -> {
            // Si estamos actualizando y es el mismo cliente, no lanzar excepción
            if (clienteIdExcluir != null &&
                clienteExistente.getClienteId().valor().equals(clienteIdExcluir)) {
                return;
            }

            throw new EmailDuplicadoExcepcion(email, clienteExistente.getClienteId().valor());
        });
    }

    /**
     * Valida que un DNI sea único en el sistema.
     *
     * @param dni DNI a validar (normalizado)
     * @throws DniDuplicadoExcepcion si el DNI ya existe
     */
    private void validarDniUnico(String dni) {
        clienteRepositorio.findByDni(dni).ifPresent(clienteExistente -> {
            // Enmascarar DNI en el mensaje de error
            String dniEnmascarado = dni.substring(0, 3) + "****" + dni.substring(dni.length() - 2);
            throw new DniDuplicadoExcepcion(dniEnmascarado, clienteExistente.getClienteId().valor());
        });
    }

    /**
     * Crea una entidad TarjetaCredito desde un DTO.
     *
     * <p><strong>NOTA:</strong> En producción, aquí se encriptarían los datos sensibles
     * (número de tarjeta y CVV) antes de crear la entidad. Por ahora se usa placeholder.
     *
     * @param clienteId ID del cliente propietario
     * @param dto DTO con datos de la tarjeta
     * @return entidad TarjetaCredito creada
     */
    private TarjetaCredito crearTarjetaDesdeDTO(
            ClienteId clienteId,
            dev.javacadabra.reservasviaje.cliente.aplicacion.dto.entrada.CrearTarjetaDTO dto) {

        // TODO: Encriptar número de tarjeta y CVV antes de crear value objects
        // Por ahora usamos placeholder - los datos deberían encriptarse aquí
        NumeroTarjeta numeroTarjeta = NumeroTarjeta.de(dto.numeroTarjeta());
        CVV cvv = CVV.crear(dto.cvv());

        // Parsear fecha de expiración MM/YY
        int mes = Integer.parseInt(dto.obtenerMesExpiracion());
        int anio = dto.obtenerAnioExpiracionCompleto();
        YearMonth fechaExpiracion = YearMonth.of(anio, mes);

        return TarjetaCredito.crear(clienteId, numeroTarjeta, fechaExpiracion, cvv);
    }
}