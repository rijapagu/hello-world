<?php

declare(strict_types=1);

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

/*
|--------------------------------------------------------------------------
| Test Case
|--------------------------------------------------------------------------
| Los tests de tests/Feature arrancan la aplicación Laravel y reinician la
| base de datos (sqlite en memoria en CI) en cada test.
| Los tests de tests/Unit (incluidos los de arquitectura) son PHP puro.
*/

pest()->extend(TestCase::class)
    ->use(RefreshDatabase::class)
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
