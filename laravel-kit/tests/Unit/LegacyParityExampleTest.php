<?php

declare(strict_types=1);

use Illuminate\Support\Str;

/*
|--------------------------------------------------------------------------
| Patrón de test de PARIDAD para la migración
|--------------------------------------------------------------------------
| 1. Exporta casos reales del sistema antiguo a tests/Fixtures/legacy/*.json
|    con la forma { "nombre del caso": { "input": …, "expected": … } }.
| 2. El dataset los recorre; el test comprueba que la implementación nueva
|    devuelve exactamente lo mismo.
| 3. Cada módulo migrado añade su fixture + su test. Sin fixture, no hay PR.
|
| Aquí el "sistema antiguo" generaba slugs; la implementación nueva es Str::slug.
*/

dataset('slugs del sistema antiguo', function (): Generator {
    $json = file_get_contents(__DIR__.'/../Fixtures/legacy/slugs.json');
    $cases = json_decode((string) $json, true, 512, JSON_THROW_ON_ERROR);

    foreach ($cases as $name => $case) {
        yield $name => [$case['input'], $case['expected']];
    }
});

it('genera el mismo slug que el sistema antiguo', function (string $input, string $expected): void {
    expect(Str::slug($input))
        ->toBe($expected)
        ->toBeSlug();
})->with('slugs del sistema antiguo');
