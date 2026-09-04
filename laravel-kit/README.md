# laravel-kit — workflow git + suite de tests para migrar el ERP de Visual FoxPro a Laravel

Kit portable para instalar en el proyecto Laravel de la migración (que vive fuera de este repo) el mismo
método de trabajo que se está acordando para el resto del equipo: **ramas cortas + PR + CI en verde + un
test por cada cambio**. No inventa nada: envuelve lo que el ecosistema Laravel ya ofrece, y añade los
patrones de test propios de una migración desde Visual FoxPro.

## Cómo traerte el kit

El repositorio es público, así que no hace falta autenticarse. Con git:

```bash
git clone --depth 1 --branch claude/git-workflow-php-tests-a33too \
  https://github.com/rijapagu/hello-world.git kit
```

Sin git: entra en [el repositorio](https://github.com/rijapagu/hello-world), selecciona la rama
`claude/git-workflow-php-tests-a33too` en el desplegable de ramas y usa *Code → Download ZIP*. De todo
lo que baja solo te interesa la carpeta `laravel-kit/`; el resto es una app Android que no tiene nada
que ver.

## Instalación en tu proyecto Laravel

```bash
# desde la carpeta que acabas de clonar o descomprimir
bash laravel-kit/install.sh /ruta/a/tu-proyecto-laravel
cd /ruta/a/tu-proyecto-laravel && composer qa
```

El script copia los ficheros (sin sobrescribir los tuyos: si ya existe uno distinto deja un `.kit` al
lado), añade los scripts de Composer que falten, instala Pest + plugin Laravel, Larastan y Pint, y
activa los hooks de git. Se puede ejecutar varias veces. Con `--skip-composer` solo copia ficheros.

Requisitos: PHP ≥ 8.3 y Laravel ≥ 11 (`/up` health check, `bootstrap/app.php`). Pest 5 exige PHP 8.4;
con PHP 8.3 Composer instalará Pest 4 y todo funciona igual.

### Si trabajas en Windows

El instalador y los hooks son scripts de bash, así que ejecútalos desde **Git Bash** (viene incluido en
Git para Windows) o desde WSL. Desde CMD o PowerShell no funcionan.

- Las rutas van con barras normales: `/c/Users/tu-usuario/erp` o `C:/Users/tu-usuario/erp`.
- PHP y Composer tienen que estar en el `PATH`. Si usas Laragon o XAMPP suele bastar con abrir Git Bash
  desde su terminal, o añadir la carpeta de PHP al `PATH` de Windows.
- El kit instala un `.gitattributes` que fuerza LF en los scripts. **No lo quites**: sin él, git
  convierte los hooks a CRLF al hacer commit y la siguiente persona que clone el repositorio se
  encuentra con `bad interpreter: /usr/bin/env bash^M`, que es un error que despista mucho para lo
  tonto que es. Si tu proyecto ya tenía `.gitattributes`, copia en él estas dos líneas:

  ```
  *.sh text eol=lf
  .githooks/* text eol=lf
  ```

  Y si el repositorio ya guardaba ficheros con CRLF, normalízalos una vez con `git add --renormalize .`.

Validado (septiembre 2026) sobre un `composer create-project laravel/laravel` recién creado: Laravel 13,
PHP 8.4, Pest 5.1.3 + pest-plugin-laravel 5.0.1, Larastan 3.11 y Pint 1.30.5. `composer qa` en verde con
los 27 tests, `install.sh` idempotente, el hook `pre-commit` bloqueando un fichero mal formateado, y los
tests de paridad comprobados con una mutación deliberada del cálculo del IVA (fallan como deben).

## Qué contiene

| Fichero | Para qué |
|---|---|
| `CONCEPTO.md` | Por qué el kit es así: las decisiones y su motivo, el estado real de cada cosa y lo que aún no está decidido. El documento de traspaso; **si solo vas a leer uno, este.** |
| `CLAUDE.md` | Instrucciones que carga sola cualquier sesión de Claude Code abierta en el proyecto: invariantes, comandos y convenciones. Si ya tienes uno, el instalador deja `CLAUDE.md.kit` para fusionar. |
| `CONTRIBUTING.md` | El método de trabajo: ramas, commits, regla "cada cambio lleva su test", PRs, protección de `main`, FAQ. **Léelo antes de tu primer PR.** |
| `MIGRACION-FOXPRO.md` | Lo específico de migrar el ERP: estrategia módulo a módulo, cómo sacar los datos del DBF, las cuatro trampas que corrompen datos en silencio, los tests contra el schema legado y el checklist por módulo. |
| `.github/workflows/laravel-ci.yml` | CI con tres checks bloqueantes: Pint, Larastan, Pest (paralelo, sqlite en memoria, cobertura mínima). |
| `.github/pull_request_template.md` | Checklist de *Definition of Done* en cada PR. |
| `.github/dependabot.yml` | Actualizaciones semanales de Composer y de las actions. |
| `.githooks/pre-commit`, `pre-push` | Pint sobre lo que se va a commitear; Larastan + Pest antes de subir. Se activan con `composer hooks:install`. |
| `.gitattributes` | Fuerza LF en los scripts para que los hooks funcionen igual en Windows, macOS y Linux. |
| `pint.json` | Preset `laravel`. |
| `phpstan.neon` | Larastan nivel 5 sobre `app/`, con instrucciones para línea base en legado. |
| `composer-scripts.json` | Scripts `lint`, `lint:fix`, `analyse`, `test:dirty`, `test:coverage`, `qa`, `hooks:install` que se fusionan en tu `composer.json`. |
| `tests/Pest.php` | Configuración de Pest: `Feature` arranca la aplicación; `RefreshDatabase` viene **apagado a propósito** (sobre un schema legado lo borraría). Expectativa `toBeImporte()`. |
| `tests/Unit/Arch/ArchitectureTest.php` | Presets de arquitectura `php`, `security`, `laravel` + dos reglas propias de ejemplo. |
| `tests/Unit/Migration/FacturasParityTest.php` + `tests/Fixtures/legacy/facturas.json` | **El patrón central**: casos reales del ERP antiguo en JSON, el código nuevo debe dar lo mismo al céntimo. Fija el IVA por línea y el redondeo *half up* de VFP. |
| `tests/Unit/Migration/NormalizacionDbfTest.php` | Las cuatro trampas del DBF: registros borrados, codificación CP850/CP1252, fechas vacías e imposibles, importes sin coma flotante. |
| `tests/Support/` | Implementaciones de ejemplo que hacen verde la suite. **Sustitúyelas por tus clases de `App\`** al migrar cada módulo. |
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
