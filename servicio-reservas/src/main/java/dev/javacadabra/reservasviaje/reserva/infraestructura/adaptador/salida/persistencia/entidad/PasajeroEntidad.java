package dev.javacadabra.reservasviaje.reserva.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad JPA que representa un pasajero en la base de datos.
 */
@Entity
@Table(name = "pasajero")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasajeroEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 200)
    private String apellidos;

    @Column(name = "numero_documento", nullable = false, length = 50)
    private String numeroDocumento;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "nacionalidad", length = 100)
    private String nacionalidad;

    // Relación Many-to-One con ReservaVuelo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_vuelo_id", nullable = false)
    private ReservaVueloEntidad reservaVuelo;
}
