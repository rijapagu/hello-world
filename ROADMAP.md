# Daily Strength — Roadmap de Implementación

Estado: **F0 en curso** (esta sesión establece la base funcional offline-first).

## Fase 0 — Fundación (esta entrega)
- [x] Arquitectura técnica completa (`ARCHITECTURE.md`)
- [x] Proyecto Gradle + version catalog + Hilt + Compose + Room + Ktor
- [x] Capa de dominio completa (modelos, repos, use cases, motor de reglas, streak, avatar)
- [x] Capa de datos: Room (entities, DAOs, DB, converters), seed de 50 ejercicios
- [x] Repositorios implementados (offline-first)
- [x] Integración IA (Ktor + PromptBuilder + validador) con fallback determinista
- [x] Tema Compose (dark mode, paleta premium) + Dashboard + ViewModel
- [x] Widget Glance de streak (Quick Start) + updater
- [x] Tests de dominio (streak, generador, validador IA)

## Fase 1 — Loop de producto completo
- [x] Onboarding + Google Sign-In (Credential Manager, opcional/no bloqueante)
- [x] Navegación single-activity (NavHost) con gating por onboarding
- [x] Diálogo "¿Tennis/Padel/No?" + WorkoutScreen ejecutable (contador de reps por serie, descanso)
- [x] WorkoutCompleteScreen con celebración de streak
- [x] Pantalla de estadísticas con gráficas de progresión (Dominadas/Flexiones/Fondos/Ab Wheel)
- [x] DataStore de preferencias + feature flags
- [ ] Pantalla de librería de ejercicios + detalle (con instrucciones y errores comunes)

## Fase 2 — Enriquecedores
- [ ] Samsung Health SDK real (steps + sesiones tennis/padel) + HealthSyncWorker
- [ ] Avatar 3D Ready Player Me (SceneView/Filament) + evolución por hitos
- [ ] Animaciones 3D de ejercicios (glTF) con controles cámara/zoom/replay
- [ ] Backend proxy del AI Coach (seguro, sin claves en cliente)

## Fase 3 — Escala y pulido
- [ ] Modularización Gradle (`:core:*`, `:feature:*`)
- [ ] Avatar generado por IA (Fase 2 del avatar)
- [ ] Animaciones generadas por IA
- [ ] CI (lint, tests, build), Crashlytics, métricas de adherencia
- [ ] Localización ES/EN, accesibilidad, widgets adicionales
