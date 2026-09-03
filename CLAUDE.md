# CLAUDE.md — Guía del proyecto Daily Strength

App Android nativa de calistenia centrada en la **racha diaria**. Filosofía: **Nunca Cero** — la app
siempre funciona offline y siempre produce un workout válido.

## Comandos
```bash
./gradlew testDebugUnitTest   # tests (dominio JUnit5 + Robolectric/Room)
./gradlew detekt              # análisis estático (reporta, no bloquea)
./gradlew assembleDebug       # APK debug
```
El CI (`.github/workflows/android.yml`) ejecuta los tres en cada push/PR.

## Arquitectura (ver `ARCHITECTURE.md`)
Clean Architecture + MVVM. Regla de dependencias: `presentation → domain ← data`.
- **`domain/`**: Kotlin puro, sin Android. Modelos inmutables, interfaces de repos, use cases, lógica
  pura (`StreakCalculator`, `WorkoutGenerator`, `AvatarStageCalculator`, `AiPlanValidator`).
- **`data/`**: Room (entities/DAOs/seed de 50 ejercicios), Ktor (AI Coach), DataStore, mappers,
  implementaciones de repos. Mappers explícitos `Entity ↔ Model ↔ Dto`.
- **`presentation/`**: Compose + ViewModels que exponen `StateFlow<UiState>`. Eventos de navegación
  vía `Channel`. Dark-first.
- **`widget/`** Glance, **`work/`** WorkManager, **`di/`** Hilt.

## Invariantes que no se deben romper
- **La IA nunca inventa ejercicios**: `AiCoachRepository` valida contra la librería y devuelve `null`
  ante cualquier fallo; `GenerateWorkoutUseCase` cae a `WorkoutGenerator` (motor determinista).
- **La racha es el KPI**: lógica en `StreakCalculator` (pura, testeada). El widget la lee en O(1).
- **Offline-first**: IA, Samsung Health, avatar 3D y Google Sign-In son *enriquecedores* opcionales,
  nunca dependencias duras.

## Convenciones
- Strings de UI en español, dentro de los composables (la localización EN está pendiente).
- Enums se persisten por `name` (contrato estable; no renombrar sin migración de Room).
- Tests de dominio: JUnit5. Tests con Room/Android: Robolectric (corren bajo el motor `vintage`).
- Integraciones opcionales se cambian en `di/IntegrationsModule.kt` (un `@Binds`).
- `laravel-kit/` es un kit portable (workflow git, Pest/Pint/Larastan, CI) para el proyecto PHP/Laravel
  de la migración, que vive fuera de este repo. Su workflow no se ejecuta aquí; se valida instalándolo
  en un proyecto Laravel con `install.sh`.

## Pendiente = acción del propietario (no código)
- `SamsungHealthDataSource` real (AAR propietaria + partner). Hoy `NoopHealthDataSource`.
- Assets glTF por ejercicio (el render 3D ya está; faltan los modelos).
- Desplegar `/backend` con `ANTHROPIC_API_KEY` y apuntar `AI_BASE_URL`.
- `default_web_client_id` para activar Google Sign-In.
Ver `SETUP.md`.
