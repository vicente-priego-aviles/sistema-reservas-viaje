package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value Object que encapsula los datos específicos de un hotel.
 * Es inmutable y garantiza que todos los datos son válidos.
 */
@ValueObject
@Getter
@Builder
public class DatosHotel {

    private final String nombreHotel;
    private final String ciudad;
    private final String direccion;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;

    @Builder.Default
    private final String tipoHabitacion = "ESTANDAR";

    @Builder.Default
    private final Integer numeroHabitaciones = 1;

    @Builder.Default
    private final Integer numeroHuespedes = 1;

    /**
     * Constructor privado usado por Lombok Builder.
     * Realiza validaciones antes de crear la instancia.
     */
    private DatosHotel(
            String nombreHotel,
            String ciudad,
            String direccion,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            String tipoHabitacion,
            Integer numeroHabitaciones,
            Integer numeroHuespedes) {

        validarDatos(nombreHotel, ciudad, direccion, fechaEntrada,
                fechaSalida, tipoHabitacion, numeroHabitaciones, numeroHuespedes);

        this.nombreHotel = nombreHotel.trim();
        this.ciudad = ciudad.trim();
        this.direccion = direccion.trim();
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.tipoHabitacion = tipoHabitacion.trim().toUpperCase();
        this.numeroHabitaciones = numeroHabitaciones;
        this.numeroHuespedes = numeroHuespedes;
    }

    /**
     * Valida todos los datos del hotel.
     */
    private void validarDatos(
            String nombreHotel,
            String ciudad,
            String direccion,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            String tipoHabitacion,
            Integer numeroHabitaciones,
            Integer numeroHuespedes) {

        if (StringUtils.isBlank(nombreHotel)) {
            throw new IllegalArgumentException("El nombre del hotel es obligatorio");
        }

        if (StringUtils.isBlank(ciudad)) {
            throw new IllegalArgumentException("La ciudad es obligatoria");
        }

        if (StringUtils.isBlank(direccion)) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }

        if (fechaEntrada == null) {
            throw new IllegalArgumentException("La fecha de entrada es obligatoria");
        }

        if (fechaSalida == null) {
            throw new IllegalArgumentException("La fecha de salida es obligatoria");
        }

        if (fechaSalida.isBefore(fechaEntrada) || fechaSalida.isEqual(fechaEntrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
        }

        long diasEstancia = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        if (diasEstancia > 30) {
            throw new IllegalArgumentException("La estancia máxima permitida es de 30 días");
        }

        if (StringUtils.isBlank(tipoHabitacion)) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio");
        }

        if (numeroHabitaciones == null || numeroHabitaciones < 1) {
            throw new IllegalArgumentException("El número de habitaciones debe ser al menos 1");
        }

        if (numeroHabitaciones > 10) {
            throw new IllegalArgumentException("El número máximo de habitaciones por reserva es 10");
        }

        if (numeroHuespedes == null || numeroHuespedes < 1) {
            throw new IllegalArgumentException("El número de huéspedes debe ser al menos 1");
        }
    }

    /**
     * Calcula el número de noches de la reserva.
     */
    public long calcularNoches() {
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }
}