# Daily Strength — Arquitectura Técnica

> Sistema personal de mantenimiento de fuerza. Filosofía: **NUNCA CERO**.
> Android nativo · Kotlin · Jetpack Compose · Clean Architecture · MVVM · Hilt · Room · Ktor · Offline-First.

---

## 1. Visión de Ingeniería

Daily Strength optimiza **adherencia al hábito** por encima de la perfección fitness. Cada decisión técnica
se subordina a tres invariantes de producto:

1. **El streak nunca debe perderse por fricción técnica.** El cálculo del streak es local, determinista y
   funciona sin red.
2. **Cero fatiga de decisión.** El usuario abre el widget y arranca. La app decide el workout.
3. **Offline-first real.** Todo (perfil, librería de ejercicios, generación de workout, progreso, streak) funciona
   sin conexión. La IA y Samsung Health son *enriquecedores*, no dependencias duras.

La regla de oro de la IA: **el LLM nunca inventa ejercicios**. Solo selecciona IDs existentes en la base de datos
local. Si el LLM no está disponible o devuelve algo inválido, un **motor de reglas determinista** genera el workout.

---

## 2. Stack y Decisiones

| Capa | Tecnología | Justificación |
|------|-----------|---------------|
| Lenguaje | Kotlin 2.0 | Estándar Android, coroutines, null-safety |
| UI | Jetpack Compose + Material 3 | Declarativo, dark mode nativo, menos boilerplate |
| Arquitectura | Clean Architecture + MVVM | Separación dominio/datos/UI, testeable |
| DI | Hilt | Estándar Google, integración con Compose/WorkManager |
| Persistencia | Room | Offline-first, reactivo vía Flow |
| Red | Ktor Client (OkHttp engine) | Multiplataforma, ligero, content-negotiation con kotlinx.serialization |
| Estado | StateFlow + UiState inmutable | Unidireccional, testeable |
| Async | Coroutines + Flow | Reactividad de Room al widget |
| Widget | Jetpack Glance | Compose para widgets, refresco vía WorkManager/State |
| Background | WorkManager | Refresco de widget, sync de Samsung Health, recordatorios |
| Auth | Credential Manager + Google ID | API moderna recomendada (sustituye GoogleSignIn) |
| Avatar 3D | Ready Player Me + Filament/SceneView | Render glTF, evolución por hitos |
| Serialización | kotlinx.serialization | Sin reflexión, KMP-ready |
| Imágenes | Coil | Compose-first |
| Tests | JUnit5, Turbine, MockK, Compose UI Test, Room in-memory | Cobertura por capa |

**Decisión de modularización:** Fase 1 = módulo único `:app` con paquetes por capa estrictos
(`domain`, `data`, `presentation`, `widget`). Fase 2 = extracción a módulos Gradle (`:core:*`, `:feature:*`)
sin reescritura, porque las fronteras ya están limpias. Ver `ROADMAP.md`.

---

## 3. Capas (Clean Architecture)

```
┌───────────────────────────────────────────────────────────┐
│ PRESENTATION (Compose + ViewModel + Glance Widget)         │
│   - Screens, UiState, Events                               │
│   - ViewModels exponen StateFlow<UiState>                  │
│   - Depende SOLO de UseCases (dominio)                     │
├───────────────────────────────────────────────────────────┤
│ DOMAIN (puro Kotlin, sin Android)                          │
│   - Models (Exercise, Workout, Streak, UserProfile...)     │
│   - Repository interfaces                                  │
│   - UseCases (lógica de aplicación)                        │
│   - WorkoutGenerator (motor de reglas determinista)        │
│   - StreakCalculator (lógica pura de streak)               │
├───────────────────────────────────────────────────────────┤
│ DATA (implementaciones)                                    │
│   - Room (entities, DAOs, DB, seed de 50 ejercicios)       │
│   - Ktor (Ai Coach remote)                                 │
│   - Samsung Health source                                  │
│   - Repository implementations (mapean entity<->model)     │
│   - DataStore (preferencias, último sync)                  │
└───────────────────────────────────────────────────────────┘
```

**Regla de dependencias:** `presentation → domain ← data`. El dominio no conoce Android, Room ni Ktor.
Los modelos de dominio son inmutables (`data class`). Mappers explícitos entre `Entity ↔ Model ↔ Dto`.

---

## 4. Estructura de Carpetas

```
app/src/main/java/com/dailystrength/
├── DailyStrengthApp.kt              # @HiltAndroidApp
├── MainActivity.kt
├── domain/
│   ├── model/                       # Exercise, Workout, Streak, UserProfile, ...
│   ├── repository/                  # interfaces
│   ├── usecase/                     # GetTodayWorkout, CompleteWorkout, ...
│   ├── workout/                     # WorkoutGenerator (reglas)
│   ├── streak/                      # StreakCalculator
│   └── avatar/                      # AvatarStageCalculator
├── data/
│   ├── local/
│   │   ├── entity/                  # *Entity
│   │   ├── dao/                     # *Dao
│   │   ├── DailyStrengthDatabase.kt
│   │   ├── Converters.kt
│   │   └── seed/ExerciseSeed.kt     # 50 ejercicios
│   ├── remote/
│   │   ├── AiCoachApi.kt            # Ktor
│   │   ├── dto/                     # WorkoutPlanDto, ...
│   │   └── prompt/                  # PromptBuilder
│   ├── health/                      # Samsung Health source
│   ├── preferences/                 # DataStore
│   ├── mapper/                      # Entity<->Model<->Dto
│   └── repository/                  # *RepositoryImpl
├── presentation/
│   ├── theme/                       # Color, Type, Theme
│   ├── navigation/                  # NavGraph, Routes
│   ├── components/                  # StreakFlame, ExerciseCard, ...
│   ├── dashboard/
│   ├── workout/
│   ├── library/
│   ├── stats/
│   ├── profile/
│   └── onboarding/
├── widget/                          # Glance widgets
│   ├── StreakWidget.kt
│   ├── StreakWidgetReceiver.kt
│   └── WidgetUpdater.kt
├── work/                            # WorkManager workers
└── di/                              # Hilt modules
```

---

## 5. Esquema de Base de Datos (Room)

```
user_profile (1 fila)
  id (PK=0), name, age, heightCm, weightKg, fitnessLevel,
  equipment (Set<Equipment> via converter), googleId, createdAt

exercise
  id (PK, String slug), name, category, difficulty, description,
  instructions (List<String>), commonMistakes (List<String>),
  requiredEquipment (Set<Equipment>), targetMuscles (Set<MuscleGroup>),
  videoUrl, animationRef, isUnilateral, defaultRepsByLevel, defaultHoldSeconds

workout
  id (PK autogen), date (epochDay), category, plannedDurationMin,
  sportContext (NONE/TENNIS/PADEL), source (AI/RULE_ENGINE),
  status (PENDING/COMPLETED/SKIPPED), completedAt

workout_exercise
  id (PK), workoutId (FK), exerciseId (FK), orderIndex,
  targetSets, targetReps, targetHoldSeconds, restSeconds

completed_set
  id (PK), workoutExerciseId (FK), setNumber, reps, holdSeconds, completedAt

streak  (1 fila, denormalizado para lectura O(1) en widget)
  id (PK=0), currentStreak, longestStreak, lastCompletedEpochDay, totalWorkouts

progress_point  (serie temporal por ejercicio)
  id (PK), exerciseId (FK), epochDay, bestReps, totalVolume
```

Índices: `workout.date`, `workout_exercise.workoutId`, `completed_set.workoutExerciseId`,
`progress_point(exerciseId, epochDay)`.

El widget lee `streak` y el `workout` de hoy vía DAO `Flow`, garantizando refresco reactivo.

---

## 6. Lógica de Streak (núcleo del producto)

`StreakCalculator` (dominio, puro):

- Un día cuenta como completado si existe ≥1 `workout` con `status=COMPLETED` ese `epochDay`.
- Al completar workout: si `today == lastCompleted` → no-op; si `today == lastCompleted+1` → `current++`;
  si `today > lastCompleted+1` → `current=1` (roto). `longest=max(longest,current)`.
- **Política "Never Zero":** existe `restDayGrace` configurable (por defecto 1 día/semana) que permite
  un "rescate" sin romper el streak si el usuario completa un mini-workout de 2 min al día siguiente.
- El cálculo es independiente de zona horaria del servidor: usa `LocalDate.now(zoneId).toEpochDay()`.

---

## 7. Motor de Generación de Workouts

Dos rutas, misma salida (`WorkoutPlan` validado):

```
GenerateWorkoutUseCase
  ├─ 1. Construye WorkoutContext (perfil, sport hoy, historial, equipo, categoría sugerida)
  ├─ 2. Intenta AiCoachRepository.generatePlan(context)  [si hay red + feature flag]
  │      └─ valida: todos los exercise_id ∈ DB, equipo disponible, duración 10–20 min
  ├─ 3. Si IA falla/ inválida → WorkoutGenerator.generate(context)  [reglas deterministas]
  └─ 4. Persiste Workout PENDING + WorkoutExercises
```

**Reglas del motor determinista (`WorkoutGenerator`):**

- Selección de categoría por rotación: ciclo Pull→Push→Legs→Core→Mobility, evitando repetir la de ayer.
- Si jugó **tennis/padel hoy**: reduce carga de piernas (excluye/limita Legs pesado), añade bloque de
  Mobility/Recovery, reduce duración objetivo (10–12 min).
- Filtra ejercicios por `requiredEquipment ⊆ userEquipment` y por `difficulty ≤ fitnessLevel(+1)`.
- Volumen por nivel: Beginner 2 sets, Intermediate 3, Advanced 4. Reps base por nivel desde el ejercicio.
- Empaqueta hasta llenar la duración objetivo (estimación: sets × (tiempo set + descanso)).
- Garantía Never-Zero: siempre devuelve un plan ≥10 min aunque el equipo sea mínimo (fallback a peso corporal).

---

## 8. Arquitectura de IA (AI Coach)

```
PromptBuilder → systemPrompt + userContext(JSON) → Ktor POST /coach/plan
                                                   ↑ kotlinx.serialization
Respuesta: WorkoutPlanDto (JSON estructurado, schema-constrained)
  → AiPlanValidator:
       - exercise_id existe en ExerciseDao
       - requiredEquipment ⊆ userEquipment
       - 10 ≤ duration ≤ 20
       - sets/reps en rangos sanos
  → si válido: usar; si no: descartar y caer al motor de reglas
```

- El backend (no incluido aquí) actúa como proxy seguro al LLM con la API key del servidor; la app
  **nunca** embebe claves de proveedor. Contrato vía `WorkoutPlanDto` documentado en `data/remote/dto`.
- `BuildConfig.AI_BASE_URL` configurable; feature flag `aiEnabled` en DataStore.
- Determinismo de salida: se exige `workout_type`, `duration`, `exercises[].exercise_id|sets|target_reps`.

---

## 9. Integración Samsung Health

- `HealthDataSource` (interface en data) con impl `SamsungHealthDataSource` usando Samsung Health SDK
  (lectura de Steps, Exercise sessions con filtro tipo TENNIS/PADEL).
- Permisos gestionados; degradación elegante: si el SDK no está disponible (dispositivo no Samsung),
  `NoopHealthDataSource` y el usuario indica el deporte manualmente en el diálogo de inicio.
- Un `HealthSyncWorker` (WorkManager) consulta sesiones del día y pre-rellena `sportContext`.

---

## 10. Sistema de Avatar (escalable)

- `AvatarStageCalculator` (dominio): mapea `currentStreak/longestStreak/totalWorkouts` → `AvatarStage`
  (hitos 7/30/60/90/180/365). Cada stage define parámetros visuales (postura, definición, atletismo).
- **Fase 1:** Ready Player Me. `RpmAvatarSource` descarga glTF por `avatarId`; render con SceneView/Filament.
  La evolución se expresa con morph targets / blendshapes y selección de variantes por stage.
- **Fase 2:** avatar generado por IA — interfaz `AvatarProvider` permite intercambiar la fuente sin tocar UI.
- Estado desacoplado: `AvatarState(stage, glbUrl, params)` consumido por `AvatarRenderer` Composable.

---

## 11. Sistema de Animaciones de Ejercicio

- Cada `Exercise.animationRef` apunta a un asset glTF/animado (start → movement → end).
- `ExerciseAnimationPlayer` Composable sobre SceneView: controles rotar cámara, zoom, replay.
- Carga perezosa + caché en disco. Fallback a `videoUrl` (ExoPlayer) si no hay asset 3D.
- Interfaz `AnimationSource` preparada para animaciones generadas por IA en el futuro.

---

## 12. Widget (Glance) — feature más importante

- `StreakWidget` (Glance AppWidget) en tamaños small/medium/large vía `SizeMode.Responsive`.
- Muestra: 🔥 streak, workout de hoy + categoría, duración, botón Quick Start (deep link a WorkoutActivity),
  indicador de progreso del día.
- Estado: el widget observa Room a través de `WidgetRepository` y se actualiza con `WidgetUpdater`
  (llamado tras completar workout y por `WidgetRefreshWorker` al cambiar el día / medianoche).
- Quick Start: `actionStartActivity` con extra que abre directamente el flujo de inicio de workout.

---

## 13. Testing

- **Dominio:** unit tests puros de `StreakCalculator`, `WorkoutGenerator`, `AvatarStageCalculator`,
  `AiPlanValidator` (JUnit5, sin Android).
- **Datos:** Room in-memory para DAOs; repos con fakes; Ktor `MockEngine` para Ai Coach.
- **UI:** Compose UI tests de Dashboard y flujo de workout; semantics para el botón Start y el contador de streak.
- **Cobertura objetivo:** dominio ≥90%, repos ≥80%.

---

## 14. Flujo Principal (resumen técnico)

```
Widget(🔥) ─tap Start─▶ MainActivity(deeplink) ─▶ StartDialog("¿Tennis/Padel/No?")
  ─▶ GenerateWorkoutUseCase(sportContext) ─▶ WorkoutScreen (ejecuta sets)
  ─▶ CompleteWorkoutUseCase (guarda reps, ProgressPoints)
  ─▶ UpdateStreakUseCase ─▶ WidgetUpdater (refresco inmediato)
  ─▶ WorkoutCompleteScreen (🔥 nuevo streak + avatar)
```

Ver `ROADMAP.md` para el plan de implementación por fases.
