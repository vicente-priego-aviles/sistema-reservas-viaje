package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Value Object que encapsula los datos específicos de un vuelo.
 * Es inmutable y garantiza que todos los datos son válidos.
 */
@ValueObject
@Getter
@Builder
public class DatosVuelo implements Serializable {

    private final String numeroVuelo;
    private final String aerolinea;
    private final String origen;
    private final String destino;
    private final LocalDateTime fechaSalida;
    private final LocalDateTime fechaLlegada;

    @Builder.Default
    private final String clase = "ECONOMICA";

    @Builder.Default
    private final Integer numeroPasajeros = 1;

    /**
     * Constructor privado usado por Lombok Builder.
     * Realiza validaciones antes de crear la instancia.
     */
    private DatosVuelo(
            String numeroVuelo,
            String aerolinea,
            String origen,
            String destino,
            LocalDateTime fechaSalida,
            LocalDateTime fechaLlegada,
            String clase,
            Integer numeroPasajeros) {

        validarDatos(numeroVuelo, aerolinea, origen, destino,
                fechaSalida, fechaLlegada, clase, numeroPasajeros);

        this.numeroVuelo = numeroVuelo.trim().toUpperCase();
        this.aerolinea = aerolinea.trim();
        this.origen = origen.trim().toUpperCase();
        this.destino = destino.trim().toUpperCase();
        this.fechaSalida = fechaSalida;
        this.fechaLlegada = fechaLlegada;
        this.clase = clase.trim().toUpperCase();
        this.numeroPasajeros = numeroPasajeros;
    }

    /**
     * Valida todos los datos del vuelo.
     */
    private void validarDatos(
            String numeroVuelo,
            String aerolinea,
            String origen,
            String destino,
            LocalDateTime fechaSalida,
            LocalDateTime fechaLlegada,
            String clase,
            Integer numeroPasajeros) {

        if (StringUtils.isBlank(numeroVuelo)) {
            throw new IllegalArgumentException("El número de vuelo es obligatorio");
        }

        if (StringUtils.isBlank(aerolinea)) {
            throw new IllegalArgumentException("La aerolínea es obligatoria");
        }

        if (StringUtils.isBlank(origen)) {
            throw new IllegalArgumentException("El origen es obligatorio");
        }

        if (StringUtils.isBlank(destino)) {
            throw new IllegalArgumentException("El destino es obligatorio");
        }

        if (origen.trim().equalsIgnoreCase(destino.trim())) {
            throw new IllegalArgumentException("El origen y destino no pueden ser iguales");
        }

        if (fechaSalida == null) {
            throw new IllegalArgumentException("La fecha de salida es obligatoria");
        }

        if (fechaLlegada == null) {
            throw new IllegalArgumentException("La fecha de llegada es obligatoria");
        }

        if (fechaLlegada.isBefore(fechaSalida)) {
            throw new IllegalArgumentException("La fecha de llegada debe ser posterior a la de salida");
        }

        if (StringUtils.isBlank(clase)) {
            throw new IllegalArgumentException("La clase es obligatoria");
        }

        if (numeroPasajeros == null || numeroPasajeros < 1) {
            throw new IllegalArgumentException("El número de pasajeros debe ser al menos 1");
        }

        if (numeroPasajeros > 9) {
            throw new IllegalArgumentException("El número máximo de pasajeros por reserva es 9");
        }
    }
}