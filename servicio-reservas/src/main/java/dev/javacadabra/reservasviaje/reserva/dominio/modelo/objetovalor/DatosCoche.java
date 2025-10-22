package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Value Object que encapsula los datos específicos de un coche de alquiler.
 * Es inmutable y garantiza que todos los datos son válidos.
 */
@ValueObject
@Getter
@Builder
public class DatosCoche {

    private final String empresaAlquiler;
    private final String modeloCoche;

    @Builder.Default
    private final String categoriaCoche = "ECONOMICO";

    private final String ubicacionRecogida;
    private final String ubicacionDevolucion;
    private final LocalDateTime fechaRecogida;
    private final LocalDateTime fechaDevolucion;

    /**
     * Constructor privado usado por Lombok Builder.
     * Realiza validaciones antes de crear la instancia.
     */
    private DatosCoche(
            String empresaAlquiler,
            String modeloCoche,
            String categoriaCoche,
            String ubicacionRecogida,
            String ubicacionDevolucion,
            LocalDateTime fechaRecogida,
            LocalDateTime fechaDevolucion) {

        validarDatos(empresaAlquiler, modeloCoche, categoriaCoche,
                ubicacionRecogida, ubicacionDevolucion,
                fechaRecogida, fechaDevolucion);

        this.empresaAlquiler = empresaAlquiler.trim();
        this.modeloCoche = modeloCoche.trim();
        this.categoriaCoche = categoriaCoche.trim().toUpperCase();
        this.ubicacionRecogida = ubicacionRecogida.trim();
        this.ubicacionDevolucion = ubicacionDevolucion.trim();
        this.fechaRecogida = fechaRecogida;
        this.fechaDevolucion = fechaDevolucion;
    }

    /**
     * Valida todos los datos del coche.
     */
    private void validarDatos(
            String empresaAlquiler,
            String modeloCoche,
            String categoriaCoche,
            String ubicacionRecogida,
            String ubicacionDevolucion,
            LocalDateTime fechaRecogida,
            LocalDateTime fechaDevolucion) {

        if (StringUtils.isBlank(empresaAlquiler)) {
            throw new IllegalArgumentException("La empresa de alquiler es obligatoria");
        }

        if (StringUtils.isBlank(modeloCoche)) {
            throw new IllegalArgumentException("El modelo del coche es obligatorio");
        }

        if (StringUtils.isBlank(categoriaCoche)) {
            throw new IllegalArgumentException("La categoría del coche es obligatoria");
        }

        if (StringUtils.isBlank(ubicacionRecogida)) {
            throw new IllegalArgumentException("La ubicación de recogida es obligatoria");
        }

        if (StringUtils.isBlank(ubicacionDevolucion)) {
            throw new IllegalArgumentException("La ubicación de devolución es obligatoria");
        }

        if (fechaRecogida == null) {
            throw new IllegalArgumentException("La fecha de recogida es obligatoria");
        }

        if (fechaDevolucion == null) {
            throw new IllegalArgumentException("La fecha de devolución es obligatoria");
        }

        if (fechaDevolucion.isBefore(fechaRecogida)) {
            throw new IllegalArgumentException("La fecha de devolución debe ser posterior a la de recogida");
        }

        long diasAlquiler = ChronoUnit.DAYS.between(fechaRecogida, fechaDevolucion);
        if (diasAlquiler < 1) {
            throw new IllegalArgumentException("El alquiler debe ser de al menos 1 día");
        }

        if (diasAlquiler > 30) {
            throw new IllegalArgumentException("El período máximo de alquiler es de 30 días");
        }
    }

    /**
     * Calcula el número de días de alquiler.
     */
    public long calcularDiasAlquiler() {
        return ChronoUnit.DAYS.between(fechaRecogida, fechaDevolucion);
    }

    /**
     * Verifica si la devolución es en la misma ubicación que la recogida.
     */
    public boolean esDevolucionEnMismaUbicacion() {
        return ubicacionRecogida.equalsIgnoreCase(ubicacionDevolucion);
    }
}
