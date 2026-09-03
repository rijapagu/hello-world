<?php

declare(strict_types=1);

/*
|--------------------------------------------------------------------------
| Tests de arquitectura (pest-plugin-arch)
|--------------------------------------------------------------------------
| Los presets vienen mantenidos por Pest: no hace falta escribir reglas a
| mano para lo habitual. Debajo hay ejemplos de reglas propias.
*/

arch()->preset()->php();       // sin dd(), dump(), var_dump(), die(), eval()…
arch()->preset()->security();  // sin md5(), sha1(), extract(), unserialize() inseguros…
arch()->preset()->laravel();   // controladores con sufijo, modelos que extienden Model, sin env() fuera de config…

arch('los controladores no consultan la base de datos directamente')
    ->expect('App\Http\Controllers')
    ->not->toUse(['Illuminate\Support\Facades\DB']);

arch('los modelos no conocen la capa HTTP')
    ->expect('App\Models')
    ->not->toUse(['Illuminate\Http\Request', 'App\Http']);
