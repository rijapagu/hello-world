<?php

declare(strict_types=1);

use Tests\TestCase;

/*
|--------------------------------------------------------------------------
| Test Case
|--------------------------------------------------------------------------
| Los tests de tests/Feature arrancan la aplicación Laravel.
| Los de tests/Unit (incluidos los de arquitectura) son PHP puro.
|
| ⚠️ RefreshDatabase viene DESACTIVADO a propósito, como en un Laravel nuevo.
| Actívalo solo si tus tests corren contra una base de datos DESECHABLE:
| sqlite :memory:, o una base de test que las migraciones de Laravel puedan
| recrear entera.
|
| En una migración desde Visual FoxPro lo normal es justo lo contrario: los
| tests apuntan al schema legado, que no lo crean las migraciones sino un
| volcado del sistema antiguo. Ahí RefreshDatabase ejecuta migrate:fresh y
| BORRA TODAS LAS TABLAS, sin que ninguna migración pueda reconstruirlas.
|
| Antes de descomentar la línea, mira a qué base apuntan tus tests
| (phpunit.xml, .env.testing) y confirma que puedes perderla entera.
| Si solo la necesitan algunos tests, que cada uno declare el trait.
*/

pest()->extend(TestCase::class)
    // ->use(Illuminate\Foundation\Testing\RefreshDatabase::class)
    ->in('Feature');

/*
|--------------------------------------------------------------------------
| Expectations
|--------------------------------------------------------------------------
| Expectativas propias del dominio. Esta comprueba que un importe viaja como
| cadena decimal y no como float, que es la regla de oro del dinero en el ERP.
*/

expect()->extend('toBeImporte', function () {
    return $this->toBeString()->toMatch('/^-?\d+\.\d{2}$/');
});
