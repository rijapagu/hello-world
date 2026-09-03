<?php

declare(strict_types=1);

use Tests\Support\CalculadoraFactura;

/*
|--------------------------------------------------------------------------
| Test de PARIDAD — el patrón central de la migración
|--------------------------------------------------------------------------
| Cada módulo que portes de Visual FoxPro necesita uno de estos:
|
| 1. Exportas del ERP antiguo casos REALES con su resultado ya impreso
|    (facturas, saldos, existencias) a tests/Fixtures/legacy/<modulo>.json.
| 2. El dataset los recorre y el test exige que el código nuevo dé lo mismo.
| 3. Los casos raros valen más que los normales: abonos, descuentos,
|    varios tipos de IVA, cantidades decimales, importes negativos.
|
| No se trata de que el cálculo nuevo sea "correcto", sino de que sea EL MISMO.
| Si el sistema antiguo tiene un sesgo de redondeo, se replica y se documenta;
| cambiarlo es una decisión de negocio, no del programador que migra.
*/

dataset('facturas del ERP antiguo', function (): Generator {
    $casos = json_decode((string) file_get_contents(__DIR__.'/../../Fixtures/legacy/facturas.json'), true, 512, JSON_THROW_ON_ERROR);

    foreach ($casos as $nombre => $caso) {
        if (str_starts_with($nombre, '_')) {
            continue;
        }

        yield $nombre => [$caso['lineas'], $caso['esperado']];
    }
});

it('calcula los mismos totales que el ERP en Visual FoxPro', function (array $lineas, array $esperado): void {
    expect(CalculadoraFactura::totales($lineas))->toBe($esperado);
})->with('facturas del ERP antiguo');

it('no calcula el IVA sobre la base total', function (): void {
    // Dos líneas de 2,45 al 21%: por línea son 0,51 + 0,51 = 1,02.
    // Sobre la base total (4,90) saldría 1,03. Un céntimo que descuadra el libro de IVA.
    $lineas = [
        ['cantidad' => '1.000', 'precio' => '2.45', 'dto' => '0.00', 'iva' => 21],
        ['cantidad' => '1.000', 'precio' => '2.45', 'dto' => '0.00', 'iva' => 21],
    ];

    expect(CalculadoraFactura::totales($lineas)['iva'])
        ->toBe('1.02')
        ->not->toBe('1.03');
});
