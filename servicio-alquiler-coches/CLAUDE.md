# servicio-alquiler-coches

> Las convenciones comunes (comandos, estructura hexagonal, cómo se escribe un worker, configuración de Zeebe) están en el [CLAUDE.md](../CLAUDE.md) raíz. Este archivo documenta lo que contiene hoy este módulo.

- **Puerto**: 9083 · **BD**: H2 `coches_db` · **Paquete**: `dev.javacadabra.reservasviaje.coche`
- **Estado actual**: solo modelo de dominio. Los paquetes `camunda/` y `rest/` están vacíos; los jobs `reservar-coche` / `cancelar-coche` los atiende `servicio-reservas`.

## Dominio

- **Agregado**: `ReservaCoche` · **Objetos de valor**: `ReservaCocheId`, `CategoriaCoche`, `EstadoReservaCoche`.
- Todavía sin excepciones ni eventos de dominio.
