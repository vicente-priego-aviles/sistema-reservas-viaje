package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de entrada para crear una reserva de vuelo.
 * Contiene todos los datos necesarios para reservar un vuelo.
 */
public record ReservarVueloDTO(

        @NotBlank(message = "El ID del cliente es obligatorio")
        String clienteId,

        @NotBlank(message = "El número de vuelo es obligatorio")
        String numeroVuelo,

        @NotBlank(message = "La aerolínea es obligatoria")
        String aerolinea,

        @NotBlank(message = "El origen es obligatorio")
        String origen,

        @NotBlank(message = "El destino es obligatorio")
        String destino,

        @NotNull(message = "La fecha de salida es obligatoria")
        @Future(message = "La fecha de salida debe ser futura")
        LocalDateTime fechaSalida,

        @NotNull(message = "La fecha de llegada es obligatoria")
        @Future(message = "La fecha de llegada debe ser futura")
        LocalDateTime fechaLlegada,

        @NotBlank(message = "La clase es obligatoria")
        String clase,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
        BigDecimal precio,

        String codigoMoneda,

        @NotNull(message = "La lista de pasajeros es obligatoria")
        @NotEmpty(message = "Debe haber al menos un pasajero")
        @Valid
        List<PasajeroDTO> pasajeros,

        String observaciones,

        String codigoConfirmacion
) {

    /**
     * DTO anidado para información de pasajeros.
     */
    public record PasajeroDTO(

            @NotBlank(message = "El nombre del pasajero es obligatorio")
            String nombre,

            @NotBlank(message = "Los apellidos del pasajero son obligatorios")
            String apellidos,

            @NotBlank(message = "El número de documento es obligatorio")
            String numeroDocumento,

            @NotBlank(message = "El tipo de documento es obligatorio")
            String tipoDocumento,

            @NotNull(message = "La fecha de nacimiento es obligatoria")
            @Past(message = "La fecha de nacimiento debe ser pasada")
            LocalDate fechaNacimiento,

            String nacionalidad
    ) {}
}