package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarReservaRespuestaDTO {

    private long processInstanceKey;
    private String estado;
    private String mensaje;
}
