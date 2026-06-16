# Daily Strength

> Sistema personal de mantenimiento de fuerza. Filosofía: **NUNCA CERO**.

Daily Strength no es una app de fitness — es un sistema de adherencia. Optimiza la **consistencia diaria**
(la racha 🔥 es el KPI principal) por encima de la perfección, con sesiones de calistenia de 10–20 min,
generación de workout sin fatiga de decisión, y un widget de inicio con arranque en un toque.

## Stack
Kotlin · Jetpack Compose · Clean Architecture + MVVM · Hilt · Room · Ktor · Glance · WorkManager ·
Coroutines/StateFlow · Offline-first · Dark mode.

## Estado
Fase 0 (fundación funcional offline-first) implementada. Ver [`ROADMAP.md`](ROADMAP.md).

- Arquitectura técnica completa: [`ARCHITECTURE.md`](ARCHITECTURE.md)
- Dominio puro (racha, generador de workouts, avatar, validador de IA) con tests JUnit5
- Datos: Room (7 entidades, DAOs, seed de **50 ejercicios**), repositorios offline-first
- IA Coach vía Ktor con **fallback determinista** (la IA nunca inventa ejercicios)
- Widget Glance de racha con Quick Start
- Dashboard Compose que cierra el loop: generar → completar → racha → widget

## Build
Requiere Android Studio (Koala+) y Android SDK 34.

```bash
./gradlew assembleDebug   # compilar
./gradlew testDebugUnitTest   # tests de dominio
```

> Nota: el proyecto se desarrolla en un entorno sin Android SDK; la compilación se valida en
> Android Studio o CI. El Gradle wrapper (8.9) está incluido.

## Estructura
`domain/` (modelos, use cases, motor de reglas) · `data/` (Room, Ktor, repos) ·
`presentation/` (Compose, ViewModels, theme) · `widget/` (Glance) · `work/` (WorkManager) · `di/` (Hilt).
