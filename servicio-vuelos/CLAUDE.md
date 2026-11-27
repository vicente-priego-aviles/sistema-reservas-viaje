# servicio-vuelos

> Las convenciones comunes (comandos, estructura hexagonal, cómo se escribe un worker, configuración de Zeebe) están en el [CLAUDE.md](../CLAUDE.md) raíz. Este archivo documenta lo que contiene hoy este módulo.

- **Puerto**: 9081 · **BD**: H2 `vuelos_db` · **Paquete**: `dev.javacadabra.reservasviaje.vuelo`
- **Estado actual**: solo modelo de dominio. Los paquetes `camunda/` y `rest/` están vacíos; los jobs `reservar-vuelo` / `cancelar-vuelo` los atiende `servicio-reservas`.

## Dominio

- **Agregado**: `ReservaVuelo` · **Objetos de valor**: `ReservaVueloId`, `AsientoNumero`, `EstadoReservaVuelo`.
- **Excepciones**: `ReservaVueloNoEncontradaException`, `VueloNoDisponibleException`.
