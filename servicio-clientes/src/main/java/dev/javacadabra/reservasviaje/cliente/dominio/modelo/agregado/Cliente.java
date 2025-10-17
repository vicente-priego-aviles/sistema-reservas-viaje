package dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.cliente.dominio.modelo.objetovalor.*;
import lombok.*;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.ArrayList;
import java.util.List;

@AggregateRoot
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {

    @Identity
    private ClienteId id;
    private Nombre nombre;
    private Email email;
    private TarjetaCredito tarjetaCredito;
    private EstadoCliente estado;

    @Builder.Default
    private List<String> historicoReservas = new ArrayList<>();

    // Métodos de negocio
    public void validarTarjeta() {
        if (!tarjetaCredito.esValida()) {
            throw new TarjetaInvalidaException(id);
        }
    }

    public void iniciarProcesoReserva() {
        if (estado != EstadoCliente.ACTIVO) {
            throw new ClienteNoActivoException(id);
        }
        this.estado = EstadoCliente.EN_PROCESO_RESERVA;
    }

    public void confirmarReserva(String reservaId) {
        this.estado = EstadoCliente.RESERVA_CONFIRMADA;
        this.historicoReservas.add(reservaId);
    }

    public void revertirEstado() {
        this.estado = EstadoCliente.ACTIVO;
    }
}