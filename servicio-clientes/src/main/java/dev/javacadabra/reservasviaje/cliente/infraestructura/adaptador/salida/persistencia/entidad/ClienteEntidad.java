package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un cliente en la base de datos.
 *
 * <p>Esta es la representación de persistencia del agregado root Cliente.
 * Se separa del modelo de dominio para mantener la arquitectura hexagonal limpia.
 *
 * <p><strong>Características:</strong>
 * <ul>
 *   <li>Agregado root que gestiona la colección de tarjetas</li>
 *   <li>Relación OneToMany con TarjetaCreditoEntidad</li>
 *   <li>Cascade ALL para propagar operaciones a tarjetas</li>
 *   <li>OrphanRemoval para eliminar tarjetas sin referencia</li>
 *   <li>Índices para optimizar búsquedas por email y DNI</li>
 *   <li>Auditoría automática con fechas de creación/modificación</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>Email y DNI tienen índices únicos</li>
 *   <li>DNI se puede almacenar encriptado (opcional)</li>
 *   <li>Datos sensibles de tarjetas en entidad separada</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Entity
@Table(
        name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cliente_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_cliente_dni", columnNames = "dni")
        },
        indexes = {
                @Index(name = "idx_cliente_estado", columnList = "estado"),
                @Index(name = "idx_cliente_email", columnList = "email"),
                @Index(name = "idx_cliente_dni", columnList = "dni"),
                @Index(name = "idx_cliente_fecha_creacion", columnList = "fecha_creacion")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tarjetas"})
@EqualsAndHashCode(of = "id")
public class ClienteEntidad {

    /**
     * Identificador único del cliente (UUID).
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    // ==================== DATOS PERSONALES ====================

    /**
     * Documento Nacional de Identidad (formato: 12345678Z).
     * Único en el sistema.
     */
    @Column(name = "dni", nullable = false, length = 9, unique = true)
    private String dni;

    /**
     * Nombre del cliente.
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Apellidos del cliente.
     */
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    /**
     * Email del cliente.
     * Único en el sistema.
     */
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    /**
     * Teléfono del cliente (formato internacional).
     * Opcional.
     */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Fecha de nacimiento del cliente.
     */
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    // ==================== DIRECCIÓN ====================

    /**
     * Calle y número de la dirección.
     */
    @Column(name = "calle", nullable = false, length = 200)
    private String calle;

    /**
     * Ciudad de residencia.
     */
    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    /**
     * Código postal.
     */
    @Column(name = "codigo_postal", nullable = false, length = 10)
    private String codigoPostal;

    /**
     * Provincia o estado.
     */
    @Column(name = "provincia", nullable = false, length = 100)
    private String provincia;

    /**
     * País de residencia.
     */
    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    // ==================== ESTADO Y CONTROL ====================

    /**
     * Estado actual del cliente en el sistema.
     */
    @Column(name = "estado", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EstadoClienteEnum estado;

    /**
     * Motivo del bloqueo si el cliente está bloqueado.
     */
    @Column(name = "motivo_bloqueo", length = 500)
    private String motivoBloqueo;

    // ==================== RELACIONES ====================

    /**
     * Tarjetas de crédito del cliente.
     *
     * <p>Relación bidireccional OneToMany con cascade ALL para propagar
     * todas las operaciones (persist, merge, remove) a las tarjetas.
     *
     * <p>OrphanRemoval = true elimina automáticamente tarjetas que quedan
     * sin referencia al ser removidas de la colección.
     */
    @OneToMany(
            mappedBy = "cliente",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<TarjetaCreditoEntidad> tarjetas = new ArrayList<>();

    // ==================== AUDITORÍA ====================

    /**
     * Fecha de creación del cliente.
     */
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última modificación.
     */
    @LastModifiedDate
    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    // ==================== MÉTODOS DE GESTIÓN DE TARJETAS ====================

    /**
     * Agrega una tarjeta a la colección manteniendo la bidireccionalidad.
     *
     * @param tarjeta tarjeta a agregar
     */
    public void agregarTarjeta(TarjetaCreditoEntidad tarjeta) {
        tarjetas.add(tarjeta);
        tarjeta.setCliente(this);
    }

    /**
     * Elimina una tarjeta de la colección.
     *
     * @param tarjeta tarjeta a eliminar
     */
    public void eliminarTarjeta(TarjetaCreditoEntidad tarjeta) {
        tarjetas.remove(tarjeta);
        tarjeta.setCliente(null);
    }

    /**
     * Reemplaza toda la colección de tarjetas manteniendo la bidireccionalidad.
     *
     * @param nuevasTarjetas nueva lista de tarjetas
     */
    public void establecerTarjetas(List<TarjetaCreditoEntidad> nuevasTarjetas) {
        // Limpiar tarjetas existentes
        this.tarjetas.clear();

        // Agregar nuevas tarjetas manteniendo bidireccionalidad
        if (nuevasTarjetas != null) {
            nuevasTarjetas.forEach(this::agregarTarjeta);
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtiene el nombre completo del cliente.
     *
     * @return nombre completo
     */
    public String obtenerNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /**
     * Obtiene la cantidad de tarjetas del cliente.
     *
     * @return cantidad de tarjetas
     */
    public int getCantidadTarjetas() {
        return tarjetas != null ? tarjetas.size() : 0;
    }

    /**
     * Verifica si el cliente tiene tarjetas válidas.
     *
     * @return true si tiene al menos una tarjeta válida
     */
    public boolean tieneTarjetasValidas() {
        return tarjetas != null && tarjetas.stream().anyMatch(TarjetaCreditoEntidad::esValida);
    }

    /**
     * Enum para estados del cliente que se persiste como String en BD.
     */
    public enum EstadoClienteEnum {
        PENDIENTE_VALIDACION("Pendiente de Validación"),
        ACTIVO("Activo"),
        EN_PROCESO_RESERVA("En Proceso de Reserva"),
        RESERVA_CONFIRMADA("Reserva Confirmada"),
        BLOQUEADO("Bloqueado"),
        INACTIVO("Inactivo");

        private final String descripcion;

        EstadoClienteEnum(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // ==================== CALLBACKS JPA ====================

    /**
     * Se ejecuta antes de persistir la entidad.
     * Inicializa valores por defecto si es necesario.
     */
    @PrePersist
    protected void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaModificacion == null) {
            fechaModificacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoClienteEnum.PENDIENTE_VALIDACION;
        }
        if (tarjetas == null) {
            tarjetas = new ArrayList<>();
        }
    }

    /**
     * Se ejecuta antes de actualizar la entidad.
     */
    @PreUpdate
    protected void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
