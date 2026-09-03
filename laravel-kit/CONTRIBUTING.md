# Guía de contribución — método de trabajo con git y tests

> **TODO (equipo):** esta guía recoge la propuesta recomendada. Alinéala con lo que se acuerde en la
> sesión de "método de trabajo con git" y borra este aviso.

Objetivo: que varias personas trabajen a la vez sobre la migración a PHP/Laravel sin pisarse, y que
**cada cambio llegue a `main` con su test y con el CI en verde**. Nada más y nada menos.

## 1. Flujo de ramas (GitHub Flow)

- `main` es la única rama permanente. Siempre está desplegable y **protegida**: nadie hace push directo.
- Cada tarea vive en una rama corta creada desde `main` actualizado:

  | Prefijo | Para qué | Ejemplo |
  |---|---|---|
  | `migrate/<modulo>` | Portar un módulo del sistema antiguo | `migrate/facturacion` |
  | `feat/<tema>` | Funcionalidad nueva | `feat/export-csv` |
  | `fix/<tema>` | Corrección de bug | `fix/redondeo-iva` |
  | `chore/<tema>` | Tooling, dependencias, CI, docs | `chore/larastan-nivel-6` |

- Una rama = una tarea = un PR. Vida máxima orientativa: **una semana**. Si crece, se parte.
- Antes de abrir el PR, actualiza la rama sobre `main`:

  ```bash
  git fetch origin
  git rebase origin/main        # en tu propia rama, el rebase es seguro
  git push --force-with-lease   # nunca --force a secas
  ```

  En ramas compartidas con otra persona, usa `git merge origin/main` en lugar de rebase.

## 2. Commits (Conventional Commits)

Formato: `tipo(ámbito opcional): resumen en imperativo`.

```
feat(facturas): calcular IVA por línea
fix(clientes): normalizar NIF antes de guardar
test(facturas): paridad con el sistema antiguo para descuentos
chore(ci): subir Larastan a nivel 6
refactor(pedidos): extraer PriceCalculator
docs: explicar fixtures de paridad
```

Tipos: `feat`, `fix`, `test`, `refactor`, `chore`, `docs`, `perf`. El resumen en el idioma que acuerde el
equipo (propuesta: tipo en inglés, resumen en español). Un commit debe dejar la suite en verde.

## 3. Regla de oro: cada cambio lleva su test

Es la *Definition of Done*. Si el cambio no se puede probar, primero se hace probable.

| Tipo de cambio | Test que se exige | Dónde |
|---|---|---|
| Módulo migrado del sistema antiguo | **Test de paridad**: mismos inputs reales → misma salida. Los casos se exportan del sistema antiguo a un fixture JSON. | `tests/Fixtures/legacy/<modulo>.json` + `tests/Unit/<Modulo>ParityTest.php` (ver `LegacyParityExampleTest.php`) |
| Lógica de negocio nueva | Test unitario (PHP puro, sin arrancar Laravel si es posible) | `tests/Unit/` |
| Endpoint / controlador | Test HTTP con `getJson`/`postJson` + `assertStatus` + `assertJsonStructure` | `tests/Feature/` |
| Bug | Test que reproduce el fallo **antes** del fix (rojo → verde) | donde viva la lógica |
| Migración de BD | Test que ejecuta `migrate` + `migrate:rollback` (RefreshDatabase ya lo cubre) | `tests/Feature/` |
| Regla de arquitectura ("los controladores no usan `DB::`") | Regla `arch()` | `tests/Unit/Arch/ArchitectureTest.php` |

Cómo generar el esqueleto: `php artisan make:test --pest NombreTest` (Feature) o `--unit` (Unit).

El CI exige una **cobertura mínima** sobre `app/` (`MIN_COVERAGE` en el workflow). Se sube con el tiempo,
nunca se baja. Un PR que baja la cobertura por debajo del mínimo no se puede fusionar.

## 4. Pull Requests

1. Abre el PR contra `main` con la plantilla (`.github/pull_request_template.md`). Título en formato
   Conventional Commits: será el mensaje del squash.
2. El CI ejecuta tres checks: **Formato (Pint)**, **Análisis estático (Larastan)**, **Tests (PHP 8.4)**.
   Los tres deben estar en verde.
3. Al menos **una revisión aprobada** de otra persona. El revisor comprueba, en este orden: ¿hay test?
   ¿el test prueba lo que dice? ¿el código es el mínimo para pasar el test? ¿nombres claros?
4. Se fusiona con **Squash and merge** y se borra la rama. Historia de `main` lineal: un commit por PR.
5. Si el PR queda desactualizado respecto a `main`, GitHub avisa: rebase (sección 1) y vuelve a subir.

### Configurar la protección de `main` en GitHub (una sola vez, el propietario)

Settings → Rules → Rulesets → *New branch ruleset*:

- Target: `main` (branch name `main`). Enforcement: *Active*.
- ✅ Restrict deletions · ✅ Block force pushes · ✅ Require linear history
- ✅ Require a pull request before merging → *Required approvals: 1* · *Dismiss stale approvals*
- ✅ Require status checks to pass → añade `Formato (Pint)`, `Análisis estático (Larastan)`,
  `Tests (PHP 8.4)` · ✅ *Require branches to be up to date before merging*
- Settings → General → Pull Requests: deja solo **Allow squash merging** y marca
  *Default to pull request title* y *Automatically delete head branches*.

## 5. Antes de subir: comprobación local

```bash
composer check          # = composer lint + composer analyse + composer test
composer lint:fix       # Pint arregla el formato solo
composer test:dirty     # Pest solo para los ficheros modificados (rápido, mientras desarrollas)
composer test:coverage  # con informe de cobertura (requiere pcov o xdebug)
composer hooks:install  # activa .githooks: pre-commit (Pint) y pre-push (Larastan + Pest)
```

Los hooks se pueden saltar en una emergencia con `SKIP_HOOKS=1`, pero el CI no se salta.

## 6. Versiones y releases

- Etiqueta en `main` con semver: `git tag -a v1.4.0 -m "v1.4.0" && git push origin v1.4.0`.
- `MAJOR` si rompe compatibilidad (API, formato de datos), `MINOR` funcionalidad, `PATCH` fixes.
- Con Conventional Commits el changelog se puede generar automáticamente (p. ej. *release-please*
  o `git log v1.3.0..v1.4.0 --oneline`). Se añade cuando el equipo lo necesite, no antes.

## 7. Preguntas frecuentes

**El CI falla en `Formato (Pint)`.** `composer lint:fix`, commit, push. Instala los hooks para que no vuelva a pasar.

**El CI falla en Larastan y el aviso es de código legado que aún no toco.** Genera una línea base
(`vendor/bin/phpstan analyse --generate-baseline`) y añádela a `phpstan.neon`. La línea base solo puede
encoger: cada PR que toque ese código elimina sus entradas.

**El test de arquitectura falla por el código antiguo aún sin migrar.** Excluye ese namespace de forma
explícita y temporal: `arch()->preset()->laravel()->ignoring('App\Legacy');`.

**Tengo un conflicto al hacer rebase.** Resuélvelo fichero a fichero, `git add`, `git rebase --continue`.
Si no lo ves claro, `git rebase --abort` y pide ayuda: un rebase nunca debe hacerse a ciegas.

**Necesito subir algo urgente sin test.** No. Escribe el test más pequeño que demuestre el arreglo; suele
costar menos que discutirlo.

**¿Puedo hacer varios commits pequeños en la rama?** Sí, los que quieras: el squash los une al fusionar.

## 8. Qué NO hacer

- Push directo a `main`, `--force` sobre ramas ajenas, commits de `vendor/` o `.env`.
- Fusionar con el CI en rojo "porque es un flake". Si es un flake, se arregla el test.
- Desactivar o saltar un test para ponerlo en verde.
- Meter en un PR de migración refactors no relacionados: se pierde la trazabilidad de la paridad.
