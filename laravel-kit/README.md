# laravel-kit — workflow git + suite de tests para la migración a PHP/Laravel

Kit portable para instalar en un proyecto Laravel (el de la migración, que vive fuera de este repo) el
mismo método de trabajo que se está acordando para el resto del equipo: **ramas cortas + PR + CI en
verde + un test por cada cambio**. No inventa nada: envuelve lo que el ecosistema Laravel ya ofrece.

## Instalación en tu proyecto Laravel

```bash
# desde el clon de este repo
bash laravel-kit/install.sh /ruta/a/tu-proyecto-laravel
cd /ruta/a/tu-proyecto-laravel && composer qa
```

El script copia los ficheros (sin sobrescribir los tuyos: si ya existe uno distinto deja un `.kit` al
lado), añade los scripts de Composer que falten, instala Pest + plugin Laravel, Larastan y Pint, y
activa los hooks de git. Se puede ejecutar varias veces. Con `--skip-composer` solo copia ficheros.

Requisitos: PHP ≥ 8.3 y Laravel ≥ 11 (`/up` health check, `bootstrap/app.php`). Pest 5 exige PHP 8.4;
con PHP 8.3 Composer instalará Pest 4 y todo funciona igual.

Validado (septiembre 2026) sobre un `composer create-project laravel/laravel` recién creado: Laravel 13,
PHP 8.4, Pest 5.1.3 + pest-plugin-laravel 5.0.1, Larastan 3.11 y Pint 1.30.5. `composer qa` en verde,
`install.sh` idempotente y el hook `pre-commit` bloqueando un fichero mal formateado.

## Qué contiene

| Fichero | Para qué |
|---|---|
| `CONTRIBUTING.md` | El método de trabajo: ramas, commits, regla "cada cambio lleva su test", PRs, protección de `main`, FAQ. **Léelo primero.** |
| `.github/workflows/laravel-ci.yml` | CI con tres checks bloqueantes: Pint, Larastan, Pest (paralelo, sqlite en memoria, cobertura mínima). |
| `.github/pull_request_template.md` | Checklist de *Definition of Done* en cada PR. |
| `.github/dependabot.yml` | Actualizaciones semanales de Composer y de las actions. |
| `.githooks/pre-commit`, `pre-push` | Pint sobre lo que se va a commitear; Larastan + Pest antes de subir. Se activan con `composer hooks:install`. |
| `pint.json` | Preset `laravel`. |
| `phpstan.neon` | Larastan nivel 5 sobre `app/`, con instrucciones para línea base en legado. |
| `composer-scripts.json` | Scripts `lint`, `lint:fix`, `analyse`, `test:dirty`, `test:coverage`, `check`, `hooks:install` que se fusionan en tu `composer.json`. |
| `tests/Pest.php` | Configuración de Pest: `Feature` con `RefreshDatabase`; expectativa de ejemplo `toBeSlug()`. |
| `tests/Unit/Arch/ArchitectureTest.php` | Presets de arquitectura `php`, `security`, `laravel` + dos reglas propias de ejemplo. |
| `tests/Unit/LegacyParityExampleTest.php` + `tests/Fixtures/legacy/slugs.json` | **Patrón de test de paridad** para la migración: casos reales del sistema antiguo en JSON, la implementación nueva debe dar lo mismo. |
| `tests/Feature/HealthCheckTest.php` | Patrón de test HTTP (`GET /up`). |

## Lo que ya existe en Laravel y el kit reutiliza

| Necesidad | Herramienta (mantenida por el ecosistema) |
|---|---|
| Tests | [Pest](https://pestphp.com) (sobre PHPUnit) + `pestphp/pest-plugin-laravel`. `php artisan make:test --pest`. `--parallel`, `--dirty`, `--coverage --min=N`, `--profile`. |
| Reglas de arquitectura | `pestphp/pest-plugin-arch` (incluido con Pest): presets `php()`, `security()`, `laravel()`. |
| Formato | [Laravel Pint](https://laravel.com/docs/pint) (incluido en apps nuevas). |
| Análisis estático | [Larastan](https://github.com/larastan/larastan) (PHPStan con conocimiento de Eloquent, facades…). |
| BD de tests | sqlite `:memory:` + `RefreshDatabase` (Laravel). |
| CI | Starter workflow oficial de GitHub para Laravel (`shivammathur/setup-php`), ampliado aquí con Pint, Larastan, cobertura y artefactos. |
| Protección de rama | GitHub *Rulesets* (configuración, no código: checklist en `CONTRIBUTING.md`). |

## Parámetros que conviene ajustar

- `MIN_COVERAGE` y `PHP_VERSION` / `matrix.php` en `.github/workflows/laravel-ci.yml`.
- `level` en `phpstan.neon` (empieza en 5, sube de uno en uno).
- Namespaces legados a excluir temporalmente en `ArchitectureTest.php` con `->ignoring(...)`.

## Siguientes pasos posibles (no incluidos)

Mutation testing (`pest --mutate`), tests de navegador (Laravel Dusk / Pest Browser), changelog
automático (*release-please*), despliegue continuo desde `main`.
