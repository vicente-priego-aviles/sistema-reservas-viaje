# servicio-hoteles

> Las convenciones comunes (comandos, estructura hexagonal, cómo se escribe un worker, configuración de Zeebe) están en el [CLAUDE.md](../CLAUDE.md) raíz. Este archivo documenta lo que contiene hoy este módulo.

- **Puerto**: 9082 · **BD**: H2 `hoteles_db` · **Paquete**: `dev.javacadabra.reservasviaje.hotel`
- **Estado actual**: solo modelo de dominio. Los paquetes `camunda/` y `rest/` están vacíos; los jobs `reservar-hotel` / `cancelar-hotel` los atiende `servicio-reservas`.

## Dominio

- **Agregado**: `ReservaHotel` · **Objetos de valor**: `ReservaHotelId`, `HabitacionNumero`, `TipoHabitacion`, `EstadoReservaHotel`.
- Todavía sin excepciones ni eventos de dominio.
