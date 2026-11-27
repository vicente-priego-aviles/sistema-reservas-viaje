# servicio-clientes

> Las convenciones comunes (comandos, estructura hexagonal, cómo se escribe un worker, configuración de Zeebe) están en el [CLAUDE.md](../CLAUDE.md) raíz. Este archivo documenta lo que contiene hoy este módulo.

- **Puerto**: 9080 (`SERVER_PORT`) · **BD**: H2 `cliente_db` por defecto (`DB_URL`) · **Paquete**: `dev.javacadabra.reservasviaje.cliente`
- **Papel**: ciclo de vida del cliente y validación de la tarjeta de crédito para el proceso `subproceso-gestion-cliente`.
- `spring.application.name` es `servicio-cliente` (en singular); los logs van a `logs/servicio-cliente.log` (`LOG_FILE`).

## Configuración

Es el único módulo con perfiles: `application.yml`, `application-dev.yml` y `application-prod.yml` (PostgreSQL + Flyway). Datos de arranque en `src/main/resources/data.sql`; variables de entorno documentadas en `src/main/resources/env.example`. Incluye además Resilience4j, Micrometer/Prometheus y trazas con Zipkin; surefire/failsafe están configurados aquí y en `servicio-reservas`.

## API REST

- `DevAdminController` (`/dev`, apoyo al desarrollo): `GET /dev/clientes/atascados`, `POST /dev/clientes/{id}/reset-estado`.

## Workers de Zeebe

`validar-datos-entrada`, `cargar-datos-formulario`, `obtener-datos-cliente`, `crear-cliente`, `activar-cliente`, `validar-cliente`, `actualizar-registro-cliente`, `actualizar-estado-cliente`, `validar-tarjeta-credito`, `actualizar-informacion-tarjeta`, `notificar-tarjeta-invalida`, `iniciar-reserva-cliente`, `confirmar-reserva-cliente`, `finalizar-reserva-cliente`, `notificar-reserva-fallida`. Cada clase se llama como su tipo de job (`ValidarClienteWorker`, …); los errores de negocio usan códigos `ERROR_*` como `ERROR_CLIENTE_NO_ENCONTRADO`, `ERROR_CLIENTE_BLOQUEADO` o `ERROR_DNI_DUPLICADO`.

## Dominio

- **Agregado**: `Cliente` · **Entidad**: `TarjetaCredito`.
- **Objetos de valor**: `ClienteId`, `DatosPersonales`, `Direccion`, `EstadoCliente`, `NumeroTarjeta`, `CVV`, `TarjetaId`, `TipoTarjeta`.
- **Eventos**: `ClienteCreadoEvento`, `ClienteActivadoEvento`, `ClienteBloqueadoEvento`, `ClienteDesbloqueadoEvento`, `DatosPersonalesActualizadosEvento`, `DireccionActualizadaEvento`, `TarjetaAgregadaEvento`, `TarjetaEliminadaEvento`, `ProcesoReservaIniciadoEvento`, `ReservaConfirmadaEvento`, `ReservaFinalizadaEvento`.
- **Excepciones**: sufijo `...Excepcion` (los demás módulos usan `...Exception`): `ClienteNoEncontradoExcepcion`, `ClienteBloqueadoExcepcion`, `DniDuplicadoExcepcion`, `LimiteMaximoTarjetasExcepcion`, etc.
