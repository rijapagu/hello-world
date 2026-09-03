<?php

declare(strict_types=1);

namespace Tests\Support;

/**
 * Cálculo de los totales de una factura, replicando el comportamiento del ERP en Visual FoxPro.
 *
 * ES UN EJEMPLO: sustitúyelo por tu clase real de App\ cuando migres el módulo de facturación.
 *
 * Dos decisiones que hay que copiar del sistema antiguo, no elegir de nuevo:
 *
 * - **El IVA se calcula y redondea por línea**, y luego se suman las líneas. Calcularlo sobre la base
 *   total da un resultado distinto en céntimos y hace que el asesor rechace la migración.
 * - **El redondeo es "half up" alejándose del cero** (0,525 => 0,53), como ROUND() de VFP, no el
 *   redondeo bancario. Todo en enteros: con float, 2.45 * 0.21 ya no es 0.5145.
 */
final class CalculadoraFactura
{
    /**
     * @param  list<array{cantidad: string, precio: string, dto: string, iva: int}>  $lineas
     * @return array{base: string, iva: string, total: string}
     */
    public static function totales(array $lineas): array
    {
        $baseTotal = 0;
        $ivaTotal = 0;

        foreach ($lineas as $linea) {
            $cantidad = NormalizadorDbf::aMinimas($linea['cantidad'], 3); // milésimas: se vende por kg o metro
            $precio = NormalizadorDbf::aMinimas($linea['precio']);
            $dto = NormalizadorDbf::aMinimas($linea['dto']);

            $bruto = self::divideRedondeando($cantidad * $precio, 1000);
            $base = $bruto - self::divideRedondeando($bruto * $dto, 100 * 100);

            $baseTotal += $base;
            $ivaTotal += self::divideRedondeando($base * $linea['iva'], 100);
        }

        return [
            'base' => NormalizadorDbf::aDecimal($baseTotal),
            'iva' => NormalizadorDbf::aDecimal($ivaTotal),
            'total' => NormalizadorDbf::aDecimal($baseTotal + $ivaTotal),
        ];
    }

    /** División entera con redondeo half up alejándose del cero, como ROUND() de Visual FoxPro. */
    private static function divideRedondeando(int $numerador, int $denominador): int
    {
        $signo = $numerador < 0 ? -1 : 1;

        return $signo * intdiv(2 * abs($numerador) + $denominador, 2 * $denominador);
    }
}
