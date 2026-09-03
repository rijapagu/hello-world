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
| Añade aquí expectativas propias del dominio, p. ej. expect($x)->toBeSlug().
*/

expect()->extend('toBeSlug', function () {
    return $this->toMatch('/^[a-z0-9]+(?:-[a-z0-9]+)*$/');
});
