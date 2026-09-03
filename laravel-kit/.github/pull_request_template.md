## Qué cambia

<!-- Una o dos frases. Si migra un módulo del sistema antiguo, indica cuál. -->

## Por qué

<!-- Motivo / issue relacionado: Closes #123 -->

## Cómo se ha probado

<!-- Qué tests se han añadido o modificado y qué cubren. -->

## Checklist (Definition of Done)

- [ ] Cada cambio de comportamiento lleva su test (unitario, HTTP o de paridad con el sistema antiguo).
- [ ] `composer qa` pasa en local (Pint + Larastan + Pest).
- [ ] No se han añadido secretos, credenciales ni datos personales reales (ni en fixtures).
- [ ] Las migraciones de base de datos tienen `down()` o se indica por qué no.
- [ ] La rama está actualizada con `main` (rebase) y el título del PR sigue Conventional Commits.
