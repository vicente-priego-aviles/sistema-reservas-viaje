package dev.javacadabra.reservasviaje.reserva.dominio.modelo.entidad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.Entity;

import java.time.LocalDate;

/**
 * Entidad que representa un pasajero asociado a una reserva de vuelo.
 * Contiene información personal del pasajero necesaria para el viaje.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
public class Pasajero {

    private Long id;
    private final String nombre;
    private final String apellidos;
    private final String numeroDocumento;
    private final String tipoDocumento;
    private final LocalDate fechaNacimiento;
    private final String nacionalidad;

    /**
     * Constructor sin ID para nuevos pasajeros.
     */
    public Pasajero(
            String nombre,
            String apellidos,
            String numeroDocumento,
            String tipoDocumento,
            LocalDate fechaNacimiento,
            String nacionalidad) {

        validarDatos(nombre, apellidos, numeroDocumento, tipoDocumento, fechaNacimiento);

        this.id = null;
        this.nombre = nombre.trim();
        this.apellidos = apellidos.trim();
        this.numeroDocumento = numeroDocumento.trim().toUpperCase();
        this.tipoDocumento = tipoDocumento.trim().toUpperCase();
        this.fechaNacimiento = fechaNacimiento;
        this.nacionalidad = nacionalidad != null ? nacionalidad.trim() : null;
    }

    /**
     * Valida los datos del pasajero.
     */
    private void validarDatos(
            String nombre,
            String apellidos,
            String numeroDocumento,
            String tipoDocumento,
            LocalDate fechaNacimiento) {

        if (StringUtils.isBlank(nombre)) {
            throw new IllegalArgumentException("El nombre del pasajero es obligatorio");
        }

        if (StringUtils.isBlank(apellidos)) {
            throw new IllegalArgumentException("Los apellidos del pasajero son obligatorios");
        }

        if (StringUtils.isBlank(numeroDocumento)) {
            throw new IllegalArgumentException("El número de documento es obligatorio");
        }

        if (StringUtils.isBlank(tipoDocumento)) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }

        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }

        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
    }

    /**
     * Obtiene el nombre completo del pasajero.
     */
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /**
     * Calcula la edad del pasajero.
     */
    public int calcularEdad() {
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }

    /**
     * Verifica si el pasajero es menor de edad.
     */
    public boolean esMenorDeEdad() {
        return calcularEdad() < 18;
    }

    /**
     * Asigna un ID al pasajero (usado por la capa de infraestructura).
     */
    public void asignarId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("El pasajero ya tiene un ID asignado");
        }
        this.id = id;
    }
}
