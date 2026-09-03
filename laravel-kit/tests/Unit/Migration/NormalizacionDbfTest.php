<?php

declare(strict_types=1);

use Tests\Support\NormalizadorDbf;

/*
|--------------------------------------------------------------------------
| Las cuatro trampas de importar un DBF de Visual FoxPro
|--------------------------------------------------------------------------
| Todas corrompen datos EN SILENCIO: el importador termina sin error y los
| números salen mal semanas después. Por eso van con test desde el primer día.
*/

describe('registros borrados', function (): void {
    it('descarta los registros marcados para borrar', function (): void {
        // VFP no borra: marca el registro con un asterisco y lo deja en el fichero.
        // Si el importador los lee todos, resucitas facturas anuladas hace años.
        expect(NormalizadorDbf::estaBorrado(['_deleted' => true, 'codigo' => 'F-001']))->toBeTrue()
            ->and(NormalizadorDbf::estaBorrado(['_deleted' => false, 'codigo' => 'F-002']))->toBeFalse();
    });

    it('trata como vivo el registro cuya marca no viene', function (): void {
        expect(NormalizadorDbf::estaBorrado(['codigo' => 'F-003']))->toBeFalse();
    });
});

describe('codificación', function (): void {
    it('convierte CP850 (DBF creados en DOS) a UTF-8', function (): void {
        expect(NormalizadorDbf::texto("MU\xA5OZ, S.L.   ", 'CP850'))->toBe('MUÑOZ, S.L.');
    });

    it('convierte CP1252 (DBF tocados desde Windows) a UTF-8', function (): void {
        expect(NormalizadorDbf::texto("MU\xD1OZ, S.L.", 'CP1252'))->toBe('MUÑOZ, S.L.');
    });

    it('demuestra que equivocarse de código de página no da error, da basura', function (): void {
        // El mismo byte 0xA5 es Ñ en CP850 y ¥ en CP1252. Nada falla: la Ñ simplemente desaparece
        // de toda la base de datos. Por eso hay que verificar la codificación contra datos reales
        // (nombres con Ñ y acentos) y no fiarse del código de página del cabecero del DBF.
        expect(NormalizadorDbf::texto("MU\xA5OZ", 'CP1252'))->toBe('MU¥OZ')
            ->and(NormalizadorDbf::texto("MU\xA5OZ", 'CP850'))->toBe('MUÑOZ');
    });
});

describe('fechas', function (): void {
    it('convierte una fecha válida', function (): void {
        expect(NormalizadorDbf::fecha('20260115'))->toBe('2026-01-15');
    });

    it('convierte la fecha vacía de VFP en null, no en una fecha cero', function (): void {
        // La fecha vacía de VFP son espacios o ceros. Sin esto acaba como '0000-00-00',
        // que MySQL en modo estricto rechaza y en modo laxo guarda como basura.
        expect(NormalizadorDbf::fecha('        '))->toBeNull()
            ->and(NormalizadorDbf::fecha('00000000'))->toBeNull();
    });

    it('rechaza una fecha imposible en vez de inventarse otra', function (): void {
        // PHP "arreglaría" el 31 de septiembre convirtiéndolo en 1 de octubre.
        expect(NormalizadorDbf::fecha('20260931'))->toBeNull();
    });
});

describe('importes', function (): void {
    it('lee los importes como enteros en céntimos, nunca como float', function (): void {
        expect(NormalizadorDbf::aMinimas('  1234.50 '))->toBe(123450)
            ->and(NormalizadorDbf::aMinimas('-50.00'))->toBe(-5000)
            ->and(NormalizadorDbf::aMinimas('.50'))->toBe(50)
            ->and(NormalizadorDbf::aMinimas('7'))->toBe(700)
            ->and(NormalizadorDbf::aMinimas(''))->toBe(0);
    });

    it('vuelve a texto decimal sin pasar por float', function (): void {
        expect(NormalizadorDbf::aDecimal(123450))->toBe('1234.50')
            ->and(NormalizadorDbf::aDecimal(-6050))->toBe('-60.50')
            ->and(NormalizadorDbf::aDecimal(5))->toBe('0.05');
    });

    it('avisa en vez de truncar dinero en silencio', function (): void {
        NormalizadorDbf::aMinimas('10.999');
    })->throws(InvalidArgumentException::class);

    it('demuestra por qué el float no sirve para dinero', function (): void {
        // 2.45 * 0.21 en coma flotante es 0.5145000000000001, no 0.5145.
        // Con un millón de líneas, ese ruido se convierte en euros que no cuadran.
        expect(2.45 * 0.21)->not->toBe(0.5145);
    });
});
