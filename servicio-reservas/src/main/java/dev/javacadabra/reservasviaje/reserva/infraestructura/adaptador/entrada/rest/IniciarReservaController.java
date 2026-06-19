package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.entrada.rest;

import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.entrada.IniciarReservaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.dto.salida.IniciarReservaRespuestaDTO;
import dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.entrada.IniciarReservaCasoUso;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class IniciarReservaController {

    private final IniciarReservaCasoUso iniciarReservaCasoUso;

    @PostMapping("/iniciar")
    public ResponseEntity<IniciarReservaRespuestaDTO> iniciarReserva(@Valid @RequestBody IniciarReservaDTO dto) {
        IniciarReservaRespuestaDTO respuesta = iniciarReservaCasoUso.ejecutar(dto);
        return ResponseEntity.accepted().body(respuesta);
    }
}
