package dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.cliente.dominio.evento.*;
import dev.javacadabra.reservasviaje.cliente.dominio.excepcion.*;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.entidad.TarjetaCredito;
import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Agregado Root Cliente - Representa un cliente del sistema de Pagos de viajes.
 *
 * <p>Este agregado encapsula toda la información y comportamiento relacionado
 * con un cliente, incluyendo sus datos personales, dirección, estado y tarjetas
 * de crédito asociadas.
 *
 * <p><strong>Invariantes de negocio:</strong>
 * <ul>
 *   <li>Un cliente debe tener al menos 1 tarjeta de crédito siempre</li>
 *   <li>Un cliente puede tener máximo 3 tarjetas de crédito</li>
 *   <li>El email debe ser único en el sistema (validado en capa aplicación)</li>
 *   <li>El DNI debe ser único en el sistema (validado en capa aplicación)</li>
 *   <li>Solo se pueden hacer Pagos si el estado es ACTIVO o EN_PROCESO_RESERVA</li>
 *   <li>Un cliente bloqueado o inactivo no puede realizar operaciones</li>
 * </ul>
 *
 * <p><strong>Transiciones de estado:</strong>
 * <ul>
 *   <li>PENDIENTE_VALIDACION → ACTIVO (activar)</li>
 *   <li>ACTIVO → EN_PROCESO_RESERVA (iniciarProcesoReserva)</li>
 *   <li>EN_PROCESO_RESERVA → RESERVA_CONFIRMADA (confirmarReserva)</li>
 *   <li>RESERVA_CONFIRMADA → ACTIVO (finalizarReserva)</li>
 *   <li>ACTIVO → BLOQUEADO (bloquear)</li>
 *   <li>BLOQUEADO → ACTIVO (desbloquear)</li>
 *   <li>ACTIVO → INACTIVO (desactivar)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@AggregateRoot
@Getter
@ToString(exclude = {"tarjetas"})
@EqualsAndHashCode(of = "clienteId", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Slf4j
public class Cliente extends AbstractAggregateRoot<Cliente> {

    private static final int MINIMO_TARJETAS = 1;
    private static final int MAXIMO_TARJETAS = 3;

    /**
     * Identificador único del cliente.
     */
    private ClienteId clienteId;

    /**
     * Datos personales del cliente (DNI, nombre, apellidos, email, teléfono, fecha nacimiento).
     */
    private DatosPersonales datosPersonales;

    /**
     * Dirección postal del cliente.
     */
    private Direccion direccion;

    /**
     * Estado actual del cliente en el sistema.
     */
    private EstadoCliente estado;

    /**
     * Tarjetas de crédito asociadas al cliente.
     * Mínimo 1, máximo 3.
     */
    @Builder.Default
    private List<TarjetaCredito> tarjetas = new ArrayList<>();

    /**
     * Fecha de creación del cliente.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización de datos.
     */
    private LocalDateTime fechaActualizacion;

    /**
     * Motivo del bloqueo (si aplica).
     */
    private String motivoBloqueo;

    // ============================================
    // FACTORY METHODS
    // ============================================

    /**
     * Crea un nuevo cliente en estado PENDIENTE_VALIDACION.
     *
     * <p>El cliente se crea con al menos una tarjeta de crédito válida
     * y en estado PENDIENTE_VALIDACION, lo que requiere que valide su
     * email y teléfono antes de poder realizar Pagos.
     *
     * @param datosPersonales datos personales del cliente
     * @param direccion dirección postal del cliente
     * @param tarjetaInicial tarjeta de crédito inicial (obligatoria)
     * @return nuevo cliente creado
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public static Cliente crear(
            DatosPersonales datosPersonales,
            Direccion direccion,
            TarjetaCredito tarjetaInicial
    ) {
        log.debug("🔍 Creando nuevo cliente con email: {}", datosPersonales.getEmail());

        validarParametrosCreacion(datosPersonales, direccion, tarjetaInicial);

        ClienteId nuevoClienteId = ClienteId.generar();
        LocalDateTime ahora = LocalDateTime.now();

        List<TarjetaCredito> listaTarjetas = new ArrayList<>();
        listaTarjetas.add(tarjetaInicial);

        Cliente cliente = Cliente.builder()
                .clienteId(nuevoClienteId)
                .datosPersonales(datosPersonales)
                .direccion(direccion)
                .estado(EstadoCliente.PENDIENTE_VALIDACION)
                .tarjetas(listaTarjetas)
                .fechaCreacion(ahora)
                .fechaActualizacion(ahora)
                .motivoBloqueo(null)
                .build();

        // Publicar evento de dominio
        cliente.registerEvent(new ClienteCreadoEvento(
                nuevoClienteId,
                datosPersonales.getEmail(),
                datosPersonales.obtenerNombreCompleto(),
                datosPersonales.obtenerDniEnmascarado(),
                EstadoCliente.PENDIENTE_VALIDACION,
                1
        ));

        log.info("✅ Cliente creado exitosamente: {} - Estado: PENDIENTE_VALIDACION",
                nuevoClienteId);

        return cliente;
    }

    /**
     * Reconstruye un cliente desde valores ya existentes (útil para reconstrucción desde BD).
     *
     * @param clienteId identificador del cliente
     * @param datosPersonales datos personales
     * @param direccion dirección postal
     * @param estado estado del cliente
     * @param tarjetas lista de tarjetas
     * @param fechaCreacion fecha de creación
     * @param fechaActualizacion fecha de última actualización
     * @param motivoBloqueo motivo del bloqueo (si aplica)
     * @return cliente reconstruido
     */
    public static Cliente reconstruir(
            ClienteId clienteId,
            DatosPersonales datosPersonales,
            Direccion direccion,
            EstadoCliente estado,
            List<TarjetaCredito> tarjetas,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion,
            String motivoBloqueo
    ) {
        return Cliente.builder()
                .clienteId(clienteId)
                .datosPersonales(datosPersonales)
                .direccion(direccion)
                .estado(estado)
                .tarjetas(new ArrayList<>(tarjetas))
                .fechaCreacion(fechaCreacion)
                .fechaActualizacion(fechaActualizacion)
                .motivoBloqueo(motivoBloqueo)
                .build();
    }

    // ============================================
    // MÉTODOS DE NEGOCIO - GESTIÓN DE ESTADO
    // ============================================

    /**
     * Activa el cliente tras completar el proceso de validación.
     *
     * <p>El cliente pasa de PENDIENTE_VALIDACION a ACTIVO, lo que le
     * permite realizar Pagos de viajes.
     *
     * @throws IllegalStateException si el cliente no está en estado PENDIENTE_VALIDACION
     */
    public void activar() {
        log.debug("🔍 Intentando activar cliente: {}", clienteId);

        if (estado != EstadoCliente.PENDIENTE_VALIDACION) {
            throw new IllegalStateException(
                    String.format("No se puede activar un cliente en estado %s. " +
                                  "Solo se pueden activar clientes en estado PENDIENTE_VALIDACION", estado)
            );
        }

        this.estado = EstadoCliente.ACTIVO;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new ClienteActivadoEvento(
                clienteId,
                datosPersonales.getEmail(),
                datosPersonales.obtenerNombreCompleto()
        ));

        log.info("✅ Cliente activado exitosamente: {}", clienteId);
    }

    /**
     * Bloquea el cliente por motivos de seguridad.
     *
     * @param motivo motivo del bloqueo
     * @param requiereRevisionManual si requiere revisión manual de un administrador
     * @throws IllegalArgumentException si el motivo es nulo o vacío
     * @throws ClienteInactivoExcepcion si el cliente está inactivo
     */
    public void bloquear(String motivo, boolean requiereRevisionManual) {
        log.debug("🔍 Intentando bloquear cliente: {} - Motivo: {}", clienteId, motivo);

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del bloqueo no puede estar vacío");
        }

        if (estado == EstadoCliente.INACTIVO) {
            throw new ClienteInactivoExcepcion(clienteId.toString());
        }

        this.estado = EstadoCliente.BLOQUEADO;
        this.motivoBloqueo = motivo;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new ClienteBloqueadoEvento(
                clienteId,
                datosPersonales.getEmail(),
                motivo,
                requiereRevisionManual
        ));

        log.warn("⚠️ Cliente bloqueado: {} - Motivo: {}", clienteId, motivo);
    }

    /**
     * Desbloquea el cliente previamente bloqueado.
     *
     * @param motivoDesbloqueo justificación del desbloqueo
     * @param administrador administrador que autoriza el desbloqueo
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException si el cliente no está bloqueado
     */
    public void desbloquear(String motivoDesbloqueo, String administrador) {
        log.debug("🔍 Intentando desbloquear cliente: {} por {}", clienteId, administrador);

        if (motivoDesbloqueo == null || motivoDesbloqueo.isBlank()) {
            throw new IllegalArgumentException("El motivo del desbloqueo no puede estar vacío");
        }

        if (administrador == null || administrador.isBlank()) {
            throw new IllegalArgumentException("Se debe especificar el administrador que desbloquea");
        }

        if (estado != EstadoCliente.BLOQUEADO) {
            throw new IllegalStateException(
                    String.format("No se puede desbloquear un cliente en estado %s. " +
                                  "Solo se pueden desbloquear clientes BLOQUEADOS", estado)
            );
        }

        String motivoBloqueoOriginal = this.motivoBloqueo;
        this.estado = EstadoCliente.ACTIVO;
        this.motivoBloqueo = null;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new ClienteDesbloqueadoEvento(
                clienteId,
                datosPersonales.getEmail(),
                motivoBloqueoOriginal,
                motivoDesbloqueo,
                administrador
        ));

        log.info("✅ Cliente desbloqueado exitosamente: {} por {}", clienteId, administrador);
    }

    /**
     * Inicia el proceso de reserva de un viaje.
     *
     * @param reservaId ID de la reserva que se está iniciando
     * @throws ClienteBloqueadoExcepcion si el cliente está bloqueado
     * @throws ClienteInactivoExcepcion si el cliente está inactivo
     * @throws IllegalStateException si el cliente no está en estado ACTIVO
     */
    public void iniciarProcesoReserva(String reservaId) {
        log.debug("🔍 Iniciando proceso de reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        validarEstadoParaOperacion("iniciar proceso de reserva");

        if (estado != EstadoCliente.ACTIVO) {
            throw new IllegalStateException(
                    String.format("Solo se puede iniciar una reserva con un cliente ACTIVO. " +
                                  "Estado actual: %s", estado)
            );
        }

        EstadoCliente estadoAnterior = this.estado;
        this.estado = EstadoCliente.EN_PROCESO_RESERVA;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new ProcesoReservaIniciadoEvento(
                clienteId.valor(),
                reservaId,
                estadoAnterior.name(),
                EstadoCliente.EN_PROCESO_RESERVA.name(),
                LocalDateTime.now()
        ));

        log.info("🚀 Proceso de reserva iniciado para cliente: {} - Reserva: {}", clienteId, reservaId);
    }

    /**
     * Confirma la reserva tras un pago exitoso.
     *
     * @param reservaId ID de la reserva confirmada
     * @throws IllegalStateException si el cliente no está en EN_PROCESO_RESERVA
     */
    public void confirmarReserva(String reservaId) {
        log.debug("🔍 Confirmando reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        if (estado != EstadoCliente.EN_PROCESO_RESERVA) {
            throw new IllegalStateException(
                    String.format("Solo se puede confirmar una reserva si está EN_PROCESO_RESERVA. " +
                                  "Estado actual: %s", estado)
            );
        }

        EstadoCliente estadoAnterior = this.estado;
        this.estado = EstadoCliente.RESERVA_CONFIRMADA;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new ReservaConfirmadaEvento(
                clienteId.valor(),
                reservaId,
                estadoAnterior.name(),
                EstadoCliente.RESERVA_CONFIRMADA.name(),
                LocalDateTime.now()
        ));

        log.info("✅ Reserva confirmada para cliente: {} - Reserva: {}", clienteId, reservaId);
    }

    /**
     * Cancela un proceso de reserva en curso, devolviendo al cliente a ACTIVO.
     * Usado en flujos de compensación cuando el pago falla y las reservas se revierten.
     *
     * @param reservaId ID de la reserva cancelada
     * @throws IllegalStateException si el cliente no está en EN_PROCESO_RESERVA
     */
    public void cancelarProcesoReserva(String reservaId) {
        log.debug("🔍 Cancelando proceso de reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        if (estado != EstadoCliente.EN_PROCESO_RESERVA) {
            throw new IllegalStateException(
                    String.format("Solo se puede cancelar un proceso de reserva en estado EN_PROCESO_RESERVA. Estado actual: %s", estado)
            );
        }

        EstadoCliente estadoAnterior = this.estado;
        this.estado = EstadoCliente.ACTIVO;
        this.fechaActualizacion = LocalDateTime.now();

        log.info("↩️ Proceso de reserva cancelado para cliente: {} - {} → ACTIVO", clienteId, estadoAnterior);
    }

    /**
     * Finaliza el proceso de reserva, devolviendo al cliente a estado ACTIVO.
     *
     * @param reservaId ID de la reserva finalizada
     * @throws IllegalStateException si el cliente no está en RESERVA_CONFIRMADA
     */
    public void finalizarReserva(String reservaId) {
        log.debug("🔍 Finalizando reserva para cliente: {} - Reserva: {}", clienteId, reservaId);

        if (estado != EstadoCliente.RESERVA_CONFIRMADA) {
            throw new IllegalStateException(
                    String.format("Solo se puede finalizar una reserva si está CONFIRMADA. " +
                                  "Estado actual: %s", estado)
            );
        }

        EstadoCliente estadoAnterior = this.estado;
        this.estado = EstadoCliente.ACTIVO;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new ReservaFinalizadaEvento(
                clienteId.valor(),
                reservaId,
                estadoAnterior.name(),
                EstadoCliente.ACTIVO.name(),
                LocalDateTime.now()
        ));

        log.info("🏁 Reserva finalizada para cliente: {} - Reserva: {}", clienteId, reservaId);
    }

    /**
     * Desactiva el cliente (baja voluntaria).
     *
     * @throws ClienteBloqueadoExcepcion si el cliente está bloqueado
     */
    public void desactivar() {
        log.debug("🔍 Desactivando cliente: {}", clienteId);

        if (estado == EstadoCliente.BLOQUEADO) {
            throw new ClienteBloqueadoExcepcion(
                    clienteId.toString(),
                    motivoBloqueo != null ? motivoBloqueo : "Cliente bloqueado"
            );
        }

        this.estado = EstadoCliente.INACTIVO;
        this.fechaActualizacion = LocalDateTime.now();

        log.info("⏸️ Cliente desactivado: {}", clienteId);
    }

    // ============================================
    // MÉTODOS DE NEGOCIO - GESTIÓN DE TARJETAS
    // ============================================

    /**
     * Agrega una nueva tarjeta de crédito al cliente.
     *
     * @param tarjeta tarjeta a agregar
     * @throws LimiteMaximoTarjetasExcepcion si ya tiene 3 tarjetas
     * @throws ClienteBloqueadoExcepcion si el cliente está bloqueado
     * @throws ClienteInactivoExcepcion si el cliente está inactivo
     * @throws IllegalArgumentException si la tarjeta es nula
     */
    public void agregarTarjeta(TarjetaCredito tarjeta) {
        log.debug("🔍 Agregando tarjeta al cliente: {}", clienteId);

        validarEstadoParaOperacion("agregar tarjeta");

        if (tarjeta == null) {
            throw new IllegalArgumentException("La tarjeta no puede ser nula");
        }

        if (tarjetas.size() >= MAXIMO_TARJETAS) {
            throw new LimiteMaximoTarjetasExcepcion(clienteId.toString(), tarjetas.size());
        }

        tarjetas.add(tarjeta);
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new TarjetaAgregadaEvento(
                clienteId,
                tarjeta.getTarjetaId(),
                tarjeta.getTipoTarjeta(),
                tarjeta.obtenerNumeroEnmascarado().substring(
                        Math.max(0, tarjeta.obtenerNumeroEnmascarado().length() - 4)
                ),
                tarjetas.size()
        ));

        log.info("✅ Tarjeta agregada exitosamente al cliente: {} - Total tarjetas: {}",
                clienteId, tarjetas.size());
    }

    /**
     * Elimina una tarjeta de crédito del cliente.
     *
     * @param tarjetaId identificador de la tarjeta a eliminar
     * @param motivo motivo de la eliminación
     * @throws TarjetaNoEncontradaExcepcion si la tarjeta no existe
     * @throws ClienteRequiereTarjetaExcepcion si es la única tarjeta
     * @throws ClienteBloqueadoExcepcion si el cliente está bloqueado
     * @throws ClienteInactivoExcepcion si el cliente está inactivo
     */
    public void eliminarTarjeta(TarjetaId tarjetaId, String motivo) {
        log.debug("🔍 Eliminando tarjeta {} del cliente: {}", tarjetaId, clienteId);

        validarEstadoParaOperacion("eliminar tarjeta");

        if (tarjetas.size() <= MINIMO_TARJETAS) {
            throw new ClienteRequiereTarjetaExcepcion(
                    clienteId.toString(),
                    tarjetaId.toString()
            );
        }

        TarjetaCredito tarjetaAEliminar = tarjetas.stream()
                .filter(t -> t.getTarjetaId().equals(tarjetaId))
                .findFirst()
                .orElseThrow(() -> new TarjetaNoEncontradaExcepcion(
                        tarjetaId.toString(),
                        clienteId.toString()
                ));

        tarjetas.remove(tarjetaAEliminar);
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new TarjetaEliminadaEvento(
                clienteId,
                tarjetaId,
                tarjetaAEliminar.getTipoTarjeta(),
                tarjetaAEliminar.obtenerNumeroEnmascarado().substring(
                        Math.max(0, tarjetaAEliminar.obtenerNumeroEnmascarado().length() - 4)
                ),
                motivo != null ? motivo : "Sin motivo especificado",
                tarjetas.size()
        ));

        log.info("✅ Tarjeta eliminada del cliente: {} - Total tarjetas restantes: {}",
                clienteId, tarjetas.size());
    }

    /**
     * Obtiene una tarjeta específica del cliente.
     *
     * @param tarjetaId identificador de la tarjeta
     * @return tarjeta encontrada
     * @throws TarjetaNoEncontradaExcepcion si la tarjeta no existe
     */
    public TarjetaCredito obtenerTarjeta(TarjetaId tarjetaId) {
        return tarjetas.stream()
                .filter(t -> t.getTarjetaId().equals(tarjetaId))
                .findFirst()
                .orElseThrow(() -> new TarjetaNoEncontradaExcepcion(
                        tarjetaId.toString(),
                        clienteId.toString()
                ));
    }

    /**
     * Obtiene todas las tarjetas del cliente (copia inmutable).
     *
     * @return lista inmutable de tarjetas
     */
    public List<TarjetaCredito> obtenerTarjetas() {
        return Collections.unmodifiableList(tarjetas);
    }

    /**
     * Obtiene solo las tarjetas válidas del cliente.
     *
     * @return lista de tarjetas válidas
     */
    public List<TarjetaCredito> obtenerTarjetasValidas() {
        return tarjetas.stream()
                .filter(TarjetaCredito::esValida)
                .toList();
    }

    // ============================================
    // MÉTODOS DE NEGOCIO - ACTUALIZACIÓN DE DATOS
    // ============================================

    /**
     * Actualiza los datos personales del cliente.
     *
     * @param nuevosDatosPersonales nuevos datos personales
     * @throws ClienteBloqueadoExcepcion si el cliente está bloqueado
     * @throws ClienteInactivoExcepcion si el cliente está inactivo
     * @throws IllegalArgumentException si los datos son nulos
     */
    public void actualizarDatosPersonales(DatosPersonales nuevosDatosPersonales) {
        log.debug("🔍 Actualizando datos personales del cliente: {}", clienteId);

        validarEstadoParaOperacion("actualizar datos personales");

        if (nuevosDatosPersonales == null) {
            throw new IllegalArgumentException("Los nuevos datos personales no pueden ser nulos");
        }

        boolean emailModificado = !datosPersonales.getEmail().equals(nuevosDatosPersonales.getEmail());

        // Determinar campos modificados
        java.util.Set<String> camposModificados = new java.util.HashSet<>();
        if (!datosPersonales.getNombre().equals(nuevosDatosPersonales.getNombre())) {
            camposModificados.add("nombre");
        }
        if (!datosPersonales.getApellidos().equals(nuevosDatosPersonales.getApellidos())) {
            camposModificados.add("apellidos");
        }
        if (emailModificado) {
            camposModificados.add("email");
        }
        if (!Objects.equals(datosPersonales.getTelefono(), nuevosDatosPersonales.getTelefono())) {
            camposModificados.add("telefono");
        }

        this.datosPersonales = nuevosDatosPersonales;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new DatosPersonalesActualizadosEvento(
                clienteId,
                nuevosDatosPersonales.getEmail(),
                nuevosDatosPersonales.obtenerNombreCompleto(),
                camposModificados,
                emailModificado
        ));

        log.info("✅ Datos personales actualizados para cliente: {}", clienteId);
    }

    /**
     * Actualiza la dirección del cliente.
     *
     * @param nuevaDireccion nueva dirección
     * @throws ClienteBloqueadoExcepcion si el cliente está bloqueado
     * @throws ClienteInactivoExcepcion si el cliente está inactivo
     * @throws IllegalArgumentException si la dirección es nula
     */
    public void actualizarDireccion(Direccion nuevaDireccion) {
        log.debug("🔍 Actualizando dirección del cliente: {}", clienteId);

        validarEstadoParaOperacion("actualizar dirección");

        if (nuevaDireccion == null) {
            throw new IllegalArgumentException("La nueva dirección no puede ser nula");
        }

        this.direccion = nuevaDireccion;
        this.fechaActualizacion = LocalDateTime.now();

        // Publicar evento de dominio
        registerEvent(new DireccionActualizadaEvento(
                clienteId,
                nuevaDireccion.ciudad(),
                nuevaDireccion.codigoPostal(),
                nuevaDireccion.pais(),
                nuevaDireccion.obtenerDireccionResumida()
        ));

        log.info("✅ Dirección actualizada para cliente: {}", clienteId);
    }

    // ============================================
    // MÉTODOS DE CONSULTA
    // ============================================

    /**
     * Verifica si el cliente puede realizar Pagos.
     *
     * @return true si puede realizar Pagos, false en caso contrario
     */
    public boolean puedeRealizarPagos() {
        return estado.puedeRealizarPagos() && tieneTarjetasValidas();
    }

    /**
     * Verifica si el cliente tiene al menos una tarjeta válida.
     *
     * @return true si tiene tarjetas válidas, false en caso contrario
     */
    public boolean tieneTarjetasValidas() {
        return tarjetas.stream().anyMatch(TarjetaCredito::esValida);
    }

    /**
     * Verifica si el cliente está activo.
     *
     * @return true si está activo, false en caso contrario
     */
    public boolean estaActivo() {
        return estado == EstadoCliente.ACTIVO;
    }

    /**
     * Verifica si el cliente está bloqueado.
     *
     * @return true si está bloqueado, false en caso contrario
     */
    public boolean estaBloqueado() {
        return estado == EstadoCliente.BLOQUEADO;
    }

    /**
     * Verifica si el cliente está en proceso de reserva.
     *
     * @return true si está en proceso de reserva, false en caso contrario
     */
    public boolean estaEnProcesoReserva() {
        return estado == EstadoCliente.EN_PROCESO_RESERVA ||
               estado == EstadoCliente.RESERVA_CONFIRMADA;
    }

    /**
     * Obtiene la cantidad de tarjetas del cliente.
     *
     * @return cantidad de tarjetas
     */
    public int getCantidadTarjetas() {
        return tarjetas.size();
    }

    // ============================================
    // VALIDACIONES PRIVADAS
    // ============================================

    private static void validarParametrosCreacion(
            DatosPersonales datosPersonales,
            Direccion direccion,
            TarjetaCredito tarjetaInicial
    ) {
        if (datosPersonales == null) {
            throw new IllegalArgumentException("Los datos personales no pueden ser nulos");
        }

        if (direccion == null) {
            throw new IllegalArgumentException("La dirección no puede ser nula");
        }

        if (tarjetaInicial == null) {
            throw new IllegalArgumentException(
                    "Se requiere al menos una tarjeta de crédito para crear un cliente"
            );
        }
    }

    private void validarEstadoParaOperacion(String operacion) {
        if (estado == EstadoCliente.BLOQUEADO) {
            throw new ClienteBloqueadoExcepcion(
                    clienteId.toString(),
                    motivoBloqueo != null ? motivoBloqueo : "Cliente bloqueado"
            );
        }

        if (estado == EstadoCliente.INACTIVO) {
            throw new ClienteInactivoExcepcion(clienteId.toString(), operacion);
        }
    }
}