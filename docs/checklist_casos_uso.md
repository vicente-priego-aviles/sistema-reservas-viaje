# Checklist de Casos de Uso BPMN

Progreso en la verificación de todos los flujos posibles del proceso `proceso-principal`.

Marcar como ✅ una vez probado y los logs confirmen el flujo esperado.

---

## Estado de Casos

| # | Caso | Estado | Flujo BPMN cubierto |
|---|------|--------|---------------------|
| 1 | Reserva Exitosa | ✅ Probado | Happy path completo → `fin-solicitud-completada` |
| 2 | Datos de Entrada Inválidos | ✅ Probado | `ERROR_DATOS_INVALIDOS` → `fin-datos-invalidos` |
| 3 | Cliente No Encontrado | ✅ Probado | Gateway `clienteObtenido=false` → error gestión cliente |
| 4 | Cliente Bloqueado | ✅ Probado | `ERROR_CLIENTE_BLOQUEADO` en `actualizar-estado-en-proceso` → error gestión cliente |
| 5 | Tarjeta Expirada | ✅ Probado | `ERROR_TARJETA_INVALIDA` boundary en `validar-tarjeta-credito` → error gestión cliente |
| 6 | Error en Reserva con Compensación BPMN | ✅ Probado | `ERROR_VALIDACION_VUELO` → event subprocess `subproceso-manejo-errores` → BPMN compensation → gateway (`motivoFallo != null`) → notificar → `fin-reserva-fallida` |
| 7 | Error en Pago con Compensación por Mensaje | ✅ Verificado | `ERROR_PROCESAR_PAGO` → mensaje `compensar-reserva` → subproceso compensación manual → `cancelar-vuelo/hotel/coche` → `fin-reserva-no-completada` |
| 8 | Advertencia en Actualización | ⬜ Pendiente | Error en `actualizar-estado-confirmado` → `revertir-estado-cliente` → `marcar-reserva-advertencia` → `fin-reserva-con-advertencia` |
| 9 | Actualización de Tarjeta en Paralelo | ⬜ Pendiente | Mensaje no-interrumpible `tarjeta-proporcionada` → subproceso paralelo → `actualizar-informacion-tarjeta` |

---

## Subprocess Coverage

| Subproceso | Casos que lo cubren |
|------------|---------------------|
| `subproceso-gestion-cliente` | 1, 2, 3, 4, 5 |
| `subproceso-proceso-reserva` | 1, 6, 7, 9 |
| `subproceso-pago` | 1, 7, 8 |

---

## Notas de Testing

- **Caso 8**: Actualmente requiere manipulación manual de la BBDD (H2 console) ya que el boundary event en el BPMN captura `ERROR_ACTUALIZACION_CLIENTE` pero el worker lanza `ERROR_TRANSICION_INVALIDA`. Ver instrucciones detalladas en `doc_casos_uso.md`.
- **Caso 1**: Las trazas reales del `servicio-clientes` confirman: `obtener-datos-cliente` produce 13 variables; `validar-tarjeta-credito` produce 7 variables y emite `⚠️ No se proporcionó montoReserva` (esperado, usa 1.000 € como default); el DNI aparece enmascarado en logs (`123****8Z`). Ver sección "Logs a consultar" en `doc_casos_uso.md` para trazas completas.
- **Caso 6**: ✅ Probado. Para forzar el error: completar el User Task "Gestionar Reserva de Vuelo" con `pasajeros: []` vía Zeebe REST API o dejando el campo vacío en el formulario de Tasklist.
- **Todos los casos**: La respuesta HTTP 202 del REST endpoint siempre es `"estado": "INICIADA"` — el resultado real se observa en Camunda Operate.
