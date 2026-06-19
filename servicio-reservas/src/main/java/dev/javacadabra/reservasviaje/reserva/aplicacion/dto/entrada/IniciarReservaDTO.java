package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarReservaDTO {

    @NotBlank
    private String clienteId;

    private String origen;

    @NotBlank
    private String destino;

    @NotBlank
    private String fechaInicio;

    @NotBlank
    private String fechaFin;

    @Min(1)
    @Max(10)
    private int numeroPasajeros;

    @NotBlank
    private String emailContacto;

    @NotBlank
    private String telefonoContacto;
}
