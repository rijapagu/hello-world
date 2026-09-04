#!/usr/bin/env bash
# Instala el kit de workflow git + tests en un proyecto Laravel existente.
#
#   bash laravel-kit/install.sh /ruta/al/proyecto-laravel [--skip-composer]
#
# - Copia los ficheros del kit SIN sobrescribir los que ya existan (deja <fichero>.kit al lado).
# - Fusiona los scripts de composer-scripts.json en composer.json (solo añade claves que faltan).
# - Instala las herramientas de desarrollo (Pest + plugin Laravel, Larastan, Pint) con Composer.
# - Activa los hooks de git (.githooks).
# Es idempotente: puedes ejecutarlo tantas veces como quieras.
set -euo pipefail

KIT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET=""
SKIP_COMPOSER=0

for arg in "$@"; do
    case "$arg" in
        --skip-composer) SKIP_COMPOSER=1 ;;
        -h|--help) sed -n '2,11p' "$0"; exit 0 ;;
        *) TARGET="$arg" ;;
    esac
done

if [ -z "$TARGET" ]; then
    echo "Uso: bash laravel-kit/install.sh /ruta/al/proyecto-laravel [--skip-composer]" >&2
    exit 1
fi
if [ ! -f "$TARGET/artisan" ] || [ ! -f "$TARGET/composer.json" ]; then
    echo "✗ $TARGET no parece un proyecto Laravel (faltan artisan y/o composer.json)." >&2
    exit 1
fi
TARGET="$(cd "$TARGET" && pwd)"

# Ficheros que se copian tal cual (ruta relativa a la raíz del proyecto).
FILES=(
    CONCEPTO.md
    CONTRIBUTING.md
    MIGRACION-FOXPRO.md
    CLAUDE.md
    .gitattributes
    .github/workflows/laravel-ci.yml
    .github/pull_request_template.md
    .github/dependabot.yml
    .githooks/pre-commit
    .githooks/pre-push
    pint.json
    phpstan.neon
    tests/Pest.php
    tests/Support/NormalizadorDbf.php
    tests/Support/CalculadoraFactura.php
    tests/Unit/Arch/ArchitectureTest.php
    tests/Unit/Migration/FacturasParityTest.php
    tests/Unit/Migration/NormalizacionDbfTest.php
    tests/Feature/HealthCheckTest.php
    tests/Fixtures/legacy/facturas.json
)

echo "▶ Copiando ficheros del kit a $TARGET"
for rel in "${FILES[@]}"; do
    src="$KIT_DIR/$rel"
    dst="$TARGET/$rel"
    mkdir -p "$(dirname "$dst")"
    if [ ! -e "$dst" ]; then
        cp "$src" "$dst"
        echo "  + $rel"
    elif cmp -s "$src" "$dst"; then
        echo "  = $rel (sin cambios)"
    else
        cp "$src" "$dst.kit"
        echo "  ! $rel ya existe y es distinto → revisa $rel.kit y fusiona a mano"
        [ "$rel" = ".gitattributes" ] && CONFLICTO_GITATTRIBUTES=1
    fi
done
chmod +x "$TARGET/.githooks/pre-commit" "$TARGET/.githooks/pre-push"

echo "▶ Fusionando scripts en composer.json (solo claves nuevas)"
php -r '
    $target = $argv[1]; $kit = $argv[2];
    $composer = json_decode(file_get_contents("$target/composer.json"), true, 512, JSON_THROW_ON_ERROR);
    $scripts  = json_decode(file_get_contents("$kit/composer-scripts.json"), true, 512, JSON_THROW_ON_ERROR)["scripts"];
    $composer["scripts"] ??= [];
    $added = [];
    foreach ($scripts as $name => $cmd) {
        if (!array_key_exists($name, $composer["scripts"])) { $composer["scripts"][$name] = $cmd; $added[] = $name; }
    }
    if (!array_key_exists("test", $composer["scripts"])) { $composer["scripts"]["test"] = "pest --parallel"; $added[] = "test"; }
    if ($added) {
        $flags = JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE;
        file_put_contents("$target/composer.json", json_encode($composer, $flags) . "\n");
        echo "  + scripts añadidos: " . implode(", ", $added) . "\n";
    } else {
        echo "  = scripts ya presentes\n";
    }
' "$TARGET" "$KIT_DIR"

if [ "$SKIP_COMPOSER" -eq 0 ]; then
    echo "▶ Instalando herramientas de desarrollo con Composer"
    (
        cd "$TARGET"
        # Pest sustituye a PHPUnit como dependencia raíz (PHPUnit sigue instalado como dependencia de Pest).
        if composer show --locked phpunit/phpunit >/dev/null 2>&1 || grep -q '"phpunit/phpunit"' composer.json; then
            composer remove --dev phpunit/phpunit --no-update --no-interaction
        fi
        composer require --dev --with-all-dependencies --no-interaction \
            pestphp/pest pestphp/pest-plugin-laravel larastan/larastan laravel/pint
    )
else
    echo "▶ --skip-composer: recuerda ejecutar:"
    echo "    composer remove --dev phpunit/phpunit --no-update"
    echo "    composer require --dev -W pestphp/pest pestphp/pest-plugin-laravel larastan/larastan laravel/pint"
fi

if git -C "$TARGET" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    git -C "$TARGET" config core.hooksPath .githooks
    echo "▶ Hooks de git activados (core.hooksPath = .githooks)"
else
    echo "▶ $TARGET no es un repositorio git: ejecuta 'composer hooks:install' cuando lo sea."
fi

cat <<EOF

✔ Kit instalado. Comprueba que todo está en verde:
    cd $TARGET && composer qa

Pasos manuales pendientes (ver CONTRIBUTING.md):
  1. Sube el proyecto a GitHub y crea el ruleset de la rama main
     (PR obligatorio + checks "Formato (Pint)", "Análisis estático (Larastan)", "Tests (PHP 8.4)").
  2. Revisa los ficheros *.kit (si los hay) y fusiónalos con los tuyos.
  3. Ajusta MIN_COVERAGE en .github/workflows/laravel-ci.yml y 'level' en phpstan.neon a tu punto de partida.
  4. Si los tests de arquitectura fallan en código legado, usa ->ignoring('App\\Legacy') mientras migras.
EOF

if [ "${CONFLICTO_GITATTRIBUTES:-0}" = "1" ]; then
    # El proyecto ya traía su .gitattributes (Laravel incluye uno). Lo que importa no es que sea igual
    # al del kit, sino que garantice LF en los scripts: se lo preguntamos a git en vez de adivinarlo.
    eol_hooks="$(git -C "$TARGET" check-attr eol -- .githooks/pre-commit 2>/dev/null | sed 's/.*: //')"

    if [ "$eol_hooks" = "lf" ]; then
        rm -f "$TARGET/.gitattributes.kit"
        echo
        echo "✔ Tu .gitattributes ya fuerza LF en los hooks: no hay nada que fusionar."
    else
        cat <<'EOF'

⚠ Ya tenías un .gitattributes y no garantiza LF en los hooks. El del kit está en .gitattributes.kit;
  copia de él AL MENOS estas dos líneas, o fallarán en Windows con
  "bad interpreter: /usr/bin/env bash^M" en cuanto alguien vuelva a clonar el repositorio:

      *.sh text eol=lf
      .githooks/* text eol=lf

  Si el repositorio ya guardaba ficheros con CRLF, normalízalos después con:
      git add --renormalize .
EOF
    fi
fi
