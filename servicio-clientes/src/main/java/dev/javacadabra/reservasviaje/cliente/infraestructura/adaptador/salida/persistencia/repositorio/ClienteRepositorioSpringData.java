package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.repositorio;

import dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.salida.persistencia.entidad.ClienteEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositorioSpringData extends JpaRepository<ClienteEntidad, String> {

    /**
     * Busca un cliente por su email (case insensitive).
     *
     * @param email email del cliente
     * @return Optional con el cliente si existe
     */
    Optional<ClienteEntidad> findByEmailIgnoreCase(String email);

    /**
     * Busca un cliente por su DNI.
     *
     * @param dni DNI del cliente
     * @return Optional con el cliente si existe
     */
    Optional<ClienteEntidad> findByDni(String dni);

    /**
     * Verifica si existe un cliente con el email especificado (case insensitive).
     *
     * @param email email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Verifica si existe un cliente con el DNI especificado.
     *
     * @param dni DNI a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByDni(String dni);

    /**
     * Busca un cliente por su ID con todas sus tarjetas cargadas (fetch join).
     *
     * <p>Esta consulta es más eficiente que el findById estándar cuando
     * se necesitan las tarjetas, ya que evita el problema N+1.
     *
     * @param id identificador del cliente
     * @return Optional con el cliente y sus tarjetas si existe
     */
    @Query("SELECT c FROM ClienteEntidad c LEFT JOIN FETCH c.tarjetas WHERE c.id = :id")
    Optional<ClienteEntidad> findByIdWithTarjetas(@Param("id") String id);

    List<ClienteEntidad> findByEstado(ClienteEntidad.EstadoClienteEnum estado);
}