package dev.javacadabra.reservasviaje.cliente.dominio.modelo.entidad;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import lombok.*;
import org.jmolecules.ddd.annotation.Entity;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Entidad Tarjeta de Crédito dentro del agregado Cliente.
 *
 * <p>Representa una tarjeta de crédito asociada a un cliente.
 * Esta es una entidad (no un agregado) porque no tiene sentido
 * fuera del contexto de un Cliente.
 *
 * <p>Características:
 * <ul>
 *   <li>Tiene identidad propia (TarjetaId)</li>
 *   <li>Pertenece al agregado Cliente</li>
 *   <li>Número y CVV encriptados</li>
 *   <li>Validación de fecha de expiración</li>
 *   <li>Estado de validez calculado</li>
 * </ul>
 *
 * <p><strong>IMPORTANTE:</strong> Según PCI DSS, el CVV no debe
 * almacenarse en base de datos después de la autorización.
 *
 * @author javacadabra
 * @version 1.0.0
 */
@Entity
@Getter
@EqualsAndHashCode(of = "tarjetaId")
@ToString(exclude = {"numeroTarjeta", "cvv"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class TarjetaCredito {

    /**
     * Identificador único de la tarjeta.
     */
    private TarjetaId tarjetaId;

    /**
     * ID del cliente propietario de esta tarjeta.
     */
    private ClienteId clienteId;

    /**
     * Número de la tarjeta (encriptado).
     */
    private NumeroTarjeta numeroTarjeta;

    /**
     * Fecha de expiración de la tarjeta (año y mes).
     */
    private YearMonth fechaExpiracion;

    /**
     * Código CVV (encriptado).
     * ADVERTENCIA: No debe persistirse en BD según PCI DSS.
     */
    private CVV cvv;

    /**
     * Tipo de tarjeta detectado automáticamente.
     */
    private TipoTarjeta tipoTarjeta;

    /**
     * Fecha de creación del registro.
     */
    private LocalDate fechaCreacion;

    /**
     * Indica si la tarjeta fue validada externamente.
     */
    private boolean validada;

    /**
     * Motivo de rechazo si la tarjeta es inválida.
     */
    private String motivoRechazo;

    // ============================================
    // FACTORY METHODS
    // ============================================

    /**
     * Crea una nueva tarjeta de crédito.
     *
     * @param clienteId ID del cliente propietario
     * @param numeroTarjeta número de tarjeta encriptado
     * @param fechaExpiracion fecha de expiración
     * @param cvv código CVV encriptado
     * @return nueva tarjeta creada
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public static TarjetaCredito crear(
            ClienteId clienteId,
            NumeroTarjeta numeroTarjeta,
            YearMonth fechaExpiracion,
            CVV cvv
    ) {
        validarParametrosCreacion(clienteId, numeroTarjeta, fechaExpiracion, cvv);

        TipoTarjeta tipo = numeroTarjeta.detectarTipoTarjeta();
        if (tipo == null) {
            throw new IllegalArgumentException("No se pudo detectar el tipo de tarjeta");
        }

        // Validar que el CVV tenga la longitud correcta para el tipo de tarjeta
        if (!cvv.validarLongitud(tipo.getLongitudCVV())) {
            throw new IllegalArgumentException(
                    String.format("El CVV debe tener %d dígitos para tarjetas %s",
                            tipo.getLongitudCVV(), tipo.getNombre())
            );
        }

        return TarjetaCredito.builder()
                .tarjetaId(TarjetaId.generar())
                .clienteId(clienteId)
                .numeroTarjeta(numeroTarjeta)
                .fechaExpiracion(fechaExpiracion)
                .cvv(cvv)
                .tipoTarjeta(tipo)
                .fechaCreacion(LocalDate.now())
                .validada(false)
                .motivoRechazo(null)
                .build();
    }

    /**
     * Crea una tarjeta desde valores ya existentes (útil para reconstrucción desde BD).
     *
     * @param tarjetaId ID de la tarjeta
     * @param clienteId ID del cliente
     * @param numeroTarjeta número encriptado
     * @param fechaExpiracion fecha de expiración
     * @param tipoTarjeta tipo de tarjeta
     * @param fechaCreacion fecha de creación
     * @param validada si fue validada
     * @param motivoRechazo motivo de rechazo (si aplica)
     * @return tarjeta reconstruida
     */
    public static TarjetaCredito reconstruir(
            TarjetaId tarjetaId,
            ClienteId clienteId,
            NumeroTarjeta numeroTarjeta,
            YearMonth fechaExpiracion,
            TipoTarjeta tipoTarjeta,
            LocalDate fechaCreacion,
            boolean validada,
            String motivoRechazo
    ) {
        return TarjetaCredito.builder()
                .tarjetaId(tarjetaId)
                .clienteId(clienteId)
                .numeroTarjeta(numeroTarjeta)
                .fechaExpiracion(fechaExpiracion)
                .cvv(null) // El CVV nunca se persiste según PCI DSS
                .tipoTarjeta(tipoTarjeta)
                .fechaCreacion(fechaCreacion)
                .validada(validada)
                .motivoRechazo(motivoRechazo)
                .build();
    }

    // ============================================
    // MÉTODOS DE NEGOCIO
    // ============================================

    /**
     * Verifica si la tarjeta está expirada.
     *
     * @return true si está expirada, false en caso contrario
     */
    public boolean estaExpirada() {
        YearMonth ahora = YearMonth.now();
        return fechaExpiracion.isBefore(ahora);
    }

    /**
     * Verifica si la tarjeta es válida para uso.
     * Una tarjeta es válida si:
     * - No está expirada
     * - Fue validada externamente
     * - No tiene motivo de rechazo
     *
     * @return true si es válida, false en caso contrario
     */
    public boolean esValida() {
        return !estaExpirada() && validada && motivoRechazo == null;
    }

    /**
     * Marca la tarjeta como validada externamente.
     *
     * @throws IllegalStateException si la tarjeta está expirada
     */
    public void marcarComoValidada() {
        if (estaExpirada()) {
            throw new IllegalStateException(
                    "No se puede validar una tarjeta expirada. Expiró en: " + fechaExpiracion
            );
        }

        this.validada = true;
        this.motivoRechazo = null;
    }

    /**
     * Marca la tarjeta como inválida con un motivo.
     *
     * @param motivo motivo por el cual la tarjeta es inválida
     */
    public void marcarComoInvalida(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo no puede estar vacío");
        }

        this.validada = false;
        this.motivoRechazo = motivo;
    }

    /**
     * Actualiza la fecha de expiración de la tarjeta.
     *
     * @param nuevaFechaExpiracion nueva fecha de expiración
     * @throws IllegalArgumentException si la nueva fecha es anterior a la actual
     */
    public void actualizarFechaExpiracion(YearMonth nuevaFechaExpiracion) {
        if (nuevaFechaExpiracion == null) {
            throw new IllegalArgumentException("La fecha de expiración no puede ser nula");
        }

        if (nuevaFechaExpiracion.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La nueva fecha de expiración no puede estar en el pasado");
        }

        this.fechaExpiracion = nuevaFechaExpiracion;

        // Si se actualiza la fecha de expiración, invalidar la tarjeta para que se revalide
        if (this.validada) {
            this.validada = false;
            this.motivoRechazo = "Fecha de expiración actualizada - requiere revalidación";
        }
    }

    /**
     * Actualiza el número de la tarjeta.
     *
     * @param nuevoNumero nuevo número de tarjeta
     * @param nuevoCVV nuevo CVV
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public void actualizarNumeroTarjeta(NumeroTarjeta nuevoNumero, CVV nuevoCVV) {
        if (nuevoNumero == null) {
            throw new IllegalArgumentException("El nuevo número de tarjeta no puede ser nulo");
        }

        if (nuevoCVV == null) {
            throw new IllegalArgumentException("El nuevo CVV no puede ser nulo");
        }

        TipoTarjeta nuevoTipo = nuevoNumero.detectarTipoTarjeta();
        if (nuevoTipo == null) {
            throw new IllegalArgumentException("No se pudo detectar el tipo de la nueva tarjeta");
        }

        if (!nuevoCVV.validarLongitud(nuevoTipo.getLongitudCVV())) {
            throw new IllegalArgumentException(
                    String.format("El CVV debe tener %d dígitos para tarjetas %s",
                            nuevoTipo.getLongitudCVV(), nuevoTipo.getNombre())
            );
        }

        this.numeroTarjeta = nuevoNumero;
        this.cvv = nuevoCVV;
        this.tipoTarjeta = nuevoTipo;

        // Invalidar para revalidación
        this.validada = false;
        this.motivoRechazo = "Número de tarjeta actualizado - requiere revalidación";
    }

    /**
     * Obtiene el número de tarjeta enmascarado para mostrar.
     *
     * @return número enmascarado (ej: **** **** **** 1234)
     */
    public String obtenerNumeroEnmascarado() {
        return numeroTarjeta.obtenerNumeroEnmascarado();
    }

    /**
     * Calcula los meses hasta la expiración.
     *
     * @return meses hasta expiración (negativo si ya expiró)
     */
    public long mesesHastaExpiracion() {
        YearMonth ahora = YearMonth.now();
        return ahora.until(fechaExpiracion, java.time.temporal.ChronoUnit.MONTHS);
    }

    /**
     * Verifica si la tarjeta expira pronto (en los próximos 3 meses).
     *
     * @return true si expira pronto, false en caso contrario
     */
    public boolean expiraPronto() {
        long meses = mesesHastaExpiracion();
        return meses >= 0 && meses <= 3;
    }

    // ============================================
    // VALIDACIONES PRIVADAS
    // ============================================

    private static void validarParametrosCreacion(
            ClienteId clienteId,
            NumeroTarjeta numeroTarjeta,
            YearMonth fechaExpiracion,
            CVV cvv
    ) {
        if (clienteId == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        if (numeroTarjeta == null) {
            throw new IllegalArgumentException("El número de tarjeta no puede ser nulo");
        }

        if (fechaExpiracion == null) {
            throw new IllegalArgumentException("La fecha de expiración no puede ser nula");
        }

        if (cvv == null) {
            throw new IllegalArgumentException("El CVV no puede ser nulo");
        }

        // Validar que la tarjeta no esté expirada
        YearMonth ahora = YearMonth.now();
        if (fechaExpiracion.isBefore(ahora)) {
            throw new IllegalArgumentException(
                    "No se puede crear una tarjeta con fecha de expiración en el pasado: " + fechaExpiracion
            );
        }
    }
}
