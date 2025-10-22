package dev.javacadabra.reservasviaje.reserva.aplicacion.puerto.salida;

/**
 * Puerto de salida para publicación de eventos de dominio.
 * Define el contrato que debe implementar el adaptador de eventos.
 */
public interface EventoPublicadorPuerto {

    /**
     * Publica un evento de dominio.
     *
     * @param evento evento a publicar
     * @param <T> tipo del evento
     */
    <T> void publicar(T evento);

    /**
     * Publica un evento de dominio con un tema específico.
     *
     * @param tema tema o canal donde publicar el evento
     * @param evento evento a publicar
     * @param <T> tipo del evento
     */
    <T> void publicar(String tema, T evento);
}
