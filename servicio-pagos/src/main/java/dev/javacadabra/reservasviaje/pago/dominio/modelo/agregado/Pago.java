package dev.javacadabra.reservasviaje.pago.dominio.modelo.agregado;

import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.EstadoPago;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.MetodoPago;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.Monto;
import dev.javacadabra.reservasviaje.pago.dominio.modelo.objetovalor.PagoId;
import dev.javacadabra.reservasviaje.pago.dominio.excepcion.*;
import lombok.*;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDateTime;

@AggregateRoot
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pago {

    @Identity
    private PagoId id;
    private String reservaViajeId;
    private String clienteId;
    private Monto monto;
    private MetodoPago metodoPago;

    // Resultado del pago
    private String numeroTransaccion;
    private String numeroConfirmacion;
    private EstadoPago estado;
    private LocalDateTime fechaProcesamiento;
    private String mensajeError;

    // Métodos de negocio
    public void procesar() {
        validarMonto();

        // Simular procesamiento
        this.numeroTransaccion = generarNumeroTransaccion();
        this.estado = EstadoPago.PROCESADO;
        this.fechaProcesamiento = LocalDateTime.now();
    }

    public void confirmar(String numeroConfirmacion) {
        if (this.estado != EstadoPago.PROCESADO) {
            throw new IllegalStateException("El pago debe estar procesado antes de confirmar");
        }

        this.numeroConfirmacion = numeroConfirmacion;
        this.estado = EstadoPago.CONFIRMADO;
    }

    public void fallar(String mensajeError) {
        this.estado = EstadoPago.FALLIDO;
        this.mensajeError = mensajeError;
        this.fechaProcesamiento = LocalDateTime.now();
    }

    public void revertir() {
        if (this.estado != EstadoPago.PROCESADO && this.estado != EstadoPago.CONFIRMADO) {
            throw new IllegalStateException("Solo se pueden revertir pagos procesados o confirmados");
        }

        this.estado = EstadoPago.REVERTIDO;
    }

    private void validarMonto() {
        if (monto.valor() > 10000.0) {
            throw new MontoExcedeLimiteException(monto.valor());
        }

        if (monto.valor() <= 0) {
            throw new MontoInvalidoException(monto.valor());
        }
    }

    private String generarNumeroTransaccion() {
        return "TRX-" + System.currentTimeMillis();
    }

    public boolean estaConfirmado() {
        return estado == EstadoPago.CONFIRMADO;
    }
}
