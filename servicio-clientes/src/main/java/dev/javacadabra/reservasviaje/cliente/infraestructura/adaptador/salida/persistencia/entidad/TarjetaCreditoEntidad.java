package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Entidad JPA que representa una tarjeta de crédito en la base de datos.
 *
 * <p>Esta es la representación de persistencia de la entidad de dominio TarjetaCredito.
 * Se separa del modelo de dominio para mantener la arquitectura hexagonal limpia.
 *
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>El número de tarjeta se almacena encriptado</li>
 *   <li>El CVV NO se persiste en base de datos (PCI DSS compliance)</li>
 *   <li>Se mantiene solo los últimos 4 dígitos para identificación</li>
 * </ul>
 *
 * <p><strong>Relaciones:</strong>
 * <ul>
 *   <li>ManyToOne con ClienteEntidad (una tarjeta pertenece a un cliente)</li>
 *   <li>No se puede eliminar una tarjeta si el cliente solo tiene una (validado en dominio)</li>
 * </ul>
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Entity
@Table(
        name = "tarjetas_credito",
        indexes = {
                @Index(name = "idx_tarjeta_cliente", columnList = "cliente_id"),
                @Index(name = "idx_tarjeta_estado", columnList = "validada, fecha_expiracion")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cliente"})
@EqualsAndHashCode(of = "id")
public class TarjetaCreditoEntidad {

    /**
     * Identificador único de la tarjeta (UUID).
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /**
     * Referencia al cliente propietario de la tarjeta.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tarjeta_cliente"))
    private ClienteEntidad cliente;

    /**
     * Número de tarjeta encriptado.
     *
     * <p>Se almacena encriptado en base de datos usando un algoritmo seguro (AES-256).
     * Este campo contiene el número completo de la tarjeta en formato encriptado.
     */
    @Column(name = "numero_encriptado", nullable = false, length = 500)
    private String numeroEncriptado;

    /**
     * Últimos 4 dígitos de la tarjeta (sin encriptar).
     *
     * <p>Se almacena en texto plano para permitir identificación de la tarjeta
     * sin necesidad de desencriptar el número completo.
     */
    @Column(name = "ultimos_digitos", nullable = false, length = 4)
    private String ultimosDigitos;

    /**
     * Año de expiración de la tarjeta (YYYY).
     */
    @Column(name = "anio_expiracion", nullable = false)
    private Integer anioExpiracion;

    /**
     * Mes de expiración de la tarjeta (1-12).
     */
    @Column(name = "mes_expiracion", nullable = false)
    private Integer mesExpiracion;

    /**
     * Tipo de tarjeta (VISA, MASTERCARD, AMEX, DISCOVER).
     */
    @Column(name = "tipo_tarjeta", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoTarjetaEnum tipoTarjeta;

    /**
     * Indica si la tarjeta fue validada externamente con el gateway de pagos.
     */
    @Column(name = "validada", nullable = false)
    @Builder.Default
    private Boolean validada = false;

    /**
     * Motivo de rechazo si la tarjeta fue rechazada por el gateway.
     */
    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    /**
     * Fecha de creación del registro.
     */
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última modificación del registro.
     */
    @LastModifiedDate
    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtiene la fecha de expiración como YearMonth.
     *
     * @return fecha de expiración
     */
    public YearMonth obtenerFechaExpiracion() {
        return YearMonth.of(anioExpiracion, mesExpiracion);
    }

    /**
     * Establece la fecha de expiración desde un YearMonth.
     *
     * @param fechaExpiracion fecha de expiración
     */
    public void establecerFechaExpiracion(YearMonth fechaExpiracion) {
        this.anioExpiracion = fechaExpiracion.getYear();
        this.mesExpiracion = fechaExpiracion.getMonthValue();
    }

    /**
     * Verifica si la tarjeta está expirada.
     *
     * @return true si está expirada, false en caso contrario
     */
    public boolean estaExpirada() {
        YearMonth fechaExpiracion = YearMonth.of(anioExpiracion, mesExpiracion);
        return fechaExpiracion.isBefore(YearMonth.now());
    }

    /**
     * Verifica si la tarjeta es válida para uso.
     *
     * @return true si es válida (no expirada, validada, sin motivo de rechazo)
     */
    public boolean esValida() {
        return !estaExpirada() && Boolean.TRUE.equals(validada) && motivoRechazo == null;
    }

    /**
     * Enum para tipos de tarjeta que se persiste como String en BD.
     */
    public enum TipoTarjetaEnum {
        VISA("Visa"),
        MASTERCARD("Mastercard"),
        AMEX("American Express"),
        DISCOVER("Discover");

        private final String nombre;

        TipoTarjetaEnum(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    // ==================== CALLBACKS JPA ====================

    /**
     * Se ejecuta antes de persistir la entidad.
     * Valida que los datos obligatorios estén presentes.
     */
    @PrePersist
    protected void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaModificacion == null) {
            fechaModificacion = LocalDateTime.now();
        }
        if (validada == null) {
            validada = false;
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