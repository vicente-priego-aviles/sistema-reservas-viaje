package dev.javacadabra.reservasviaje.reserva.dominio.modelo.objetovalor;

import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Value Object que representa los posibles estados de una reserva.
 * Sigue el ciclo de vida definido en el proceso BPMN.
 */
@ValueObject
@Getter
public enum EstadoReserva {

    /**
     * Estado inicial cuando se crea la reserva.
     */
    PENDIENTE("Pendiente", "La reserva está pendiente de confirmación"),

    /**
     * La reserva está siendo procesada.
     */
    EN_PROCESO("En Proceso", "La reserva está siendo procesada"),

    /**
     * La reserva ha sido confirmada exitosamente.
     */
    CONFIRMADA("Confirmada", "La reserva ha sido confirmada"),

    /**
     * La reserva ha sido cancelada por el sistema o el usuario.
     */
    CANCELADA("Cancelada", "La reserva ha sido cancelada"),

    /**
     * La reserva falló durante el procesamiento.
     */
    FALLIDA("Fallida", "La reserva no pudo completarse"),

    /**
     * La reserva está confirmada pero con alguna advertencia o necesita revisión.
     */
    CONFIRMADA_CON_ADVERTENCIA("Confirmada con Advertencia",
            "La reserva está confirmada pero requiere atención");

    private final String nombre;
    private final String descripcion;

    EstadoReserva(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Verifica si el estado permite cancelación.
     *
     * @return true si se puede cancelar, false en caso contrario
     */
    public boolean permiteCancelacion() {
        return this == PENDIENTE || this == EN_PROCESO || this == CONFIRMADA;
    }

    /**
     * Verifica si el estado es terminal (no permite más cambios).
     *
     * @return true si es un estado final, false en caso contrario
     */
    public boolean esEstadoFinal() {
        return this == CONFIRMADA || this == CANCELADA || this == FALLIDA;
    }

    /**
     * Verifica si el estado indica éxito.
     *
     * @return true si la reserva fue exitosa, false en caso contrario
     */
    public boolean esExitoso() {
        return this == CONFIRMADA || this == CONFIRMADA_CON_ADVERTENCIA;
    }

    /**
     * Obtiene el estado a partir de su nombre (case-insensitive).
     *
     * @param nombre el nombre del estado
     * @return el EstadoReserva correspondiente
     * @throws IllegalArgumentException si no existe un estado con ese nombre
     */
    public static EstadoReserva desdeNombre(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del estado no puede ser nulo");
        }

        for (EstadoReserva estado : values()) {
            if (estado.name().equalsIgnoreCase(nombre.trim())) {
                return estado;
            }
        }

        throw new IllegalArgumentException(
                "No existe un estado de reserva con el nombre: " + nombre
        );
    }
}