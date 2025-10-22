package dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida con la información de una reserva de vuelo creada.
 */
public record ReservaVueloRespuestaDTO(
        String reservaId,
        String numeroVuelo,
        String aerolinea,
        String origen,
        String destino,
        LocalDateTime fechaSalida,
        LocalDateTime fechaLlegada,
        String clase,
        BigDecimal precio,
        String codigoMoneda,
        String estado,
        List<PasajeroRespuestaDTO> pasajeros,
        String clienteId,
        String codigoConfirmacion,
        LocalDateTime fechaCreacion
) {

    /**
     * DTO anidado para información de pasajeros en la respuesta.
     */
    public record PasajeroRespuestaDTO(
            Long id,
            String nombre,
            String apellidos,
            String numeroDocumento,
            String tipoDocumento
    ) {}
}
