# Daily Strength — AI Coach Backend

Proxy seguro entre la app Android y el LLM. Mantiene la **API key del proveedor en el servidor**
(la app nunca embebe claves) y expone el contrato que la app espera.

- **Stack:** Kotlin + Ktor (Netty) + SDK oficial de Anthropic (`com.anthropic:anthropic-java`).
- **Modelo:** `claude-opus-4-8` (el más capaz de la familia Opus). Para alto volumen/coste puedes
  cambiar a `Model.CLAUDE_SONNET_4_6` en `CoachService.kt`.

## Endpoint

`POST /coach/plan`

Request (igual que `CoachRequestDto` de la app):
```json
{
  "system_prompt": "…reglas del coach…",
  "profile": { "age": 41, "height_cm": 178, "weight_kg": 80, "fitness_level": "INTERMEDIATE", "equipment": ["PULL_UP_BAR"] },
  "context": { "sport_today": "PADEL", "suggested_category": "PUSH", "recent_categories": ["PULL"], "recent_best_reps": {"push_up": 20} },
  "allowed_exercise_ids": ["push_up", "incline_push_up", "dip", "pike_push_up"]
}
```

Response (igual que `WorkoutPlanDto`):
```json
{ "workout_type": "push", "duration": 14,
  "exercises": [ { "exercise_id": "push_up", "sets": 3, "target_reps": 12, "rest_seconds": 30 } ] }
```

**Garantías del servidor:** el modelo solo puede *seleccionar* de `allowed_exercise_ids`; además el
servidor **descarta cualquier id no permitido** y limita la duración a 10–20 min antes de responder.
Si no hay plan válido, devuelve `422` y la app cae a su **motor de reglas determinista**.

## Ejecutar

```bash
export ANTHROPIC_API_KEY=sk-ant-...
gradle run            # requiere Gradle 8+ y JDK 17
# escucha en :8080 (configurable con PORT)
curl localhost:8080/health
```

## Despliegue
Cualquier host de contenedores (Cloud Run, Fly.io, Render…). Variables: `ANTHROPIC_API_KEY`, `PORT`.
Apunta `BuildConfig.AI_BASE_URL` de la app a la URL pública del servicio.

> El `system_prompt` lo envía la app (`PromptBuilder`), de modo que las reglas de generación viven
> junto al cliente y el backend permanece como un proxy delgado y reemplazable.
