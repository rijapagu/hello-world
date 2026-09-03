<?php

declare(strict_types=1);

namespace Tests\Support;

use DateTimeImmutable;
use InvalidArgumentException;

/**
 * Normalización de un registro leído de un DBF de Visual FoxPro.
 *
 * ES UN EJEMPLO: sustitúyelo por tu clase real de App\ cuando escribas el importador. Lo que importa
 * no es esta implementación, sino que las cuatro reglas de abajo estén cubiertas por tests, porque
 * son las que corrompen los datos en silencio si se hacen mal.
 *
 * 1. Registros borrados: VFP no borra, marca. Si los lees todos, resucitas facturas anuladas.
 * 2. Codificación: los DBF antiguos suelen ser CP850 (DOS) y los tocados desde Windows, CP1252.
 *    El código de página del cabecero puede estar vacío o ser mentira: hay que verificarlo con datos.
 * 3. Fechas: la fecha vacía de VFP no es NULL, son ocho espacios o ceros. Y hay fechas imposibles.
 * 4. Importes: nunca float. Se trabaja en la unidad mínima (céntimos) con enteros.
 */
final class NormalizadorDbf
{
    /** Un registro marcado para borrar no debe importarse (salvo que migres también el histórico). */
    public static function estaBorrado(array $registro): bool
    {
        return ($registro['_deleted'] ?? false) === true;
    }

    /**
     * Convierte un campo de texto del DBF a UTF-8 y le quita el relleno de espacios.
     *
     * @param  string  $codepage  CP850 para DBF creados en DOS, CP1252 para los creados en Windows.
     */
    public static function texto(string $valor, string $codepage = 'CP850'): string
    {
        return trim(mb_convert_encoding($valor, 'UTF-8', $codepage));
    }

    /** Fecha DBF ('YYYYMMDD') a 'Y-m-d'. Vacía o imposible => null, nunca una fecha inventada. */
    public static function fecha(string $valor): ?string
    {
        $valor = trim($valor);

        if ($valor === '' || $valor === '00000000') {
            return null;
        }

        $fecha = DateTimeImmutable::createFromFormat('!Ymd', $valor);

        // El viaje de ida y vuelta descarta fechas que PHP "arregla" solo (20260931 => 1 de octubre).
        if (! $fecha instanceof DateTimeImmutable || $fecha->format('Ymd') !== $valor) {
            return null;
        }

        return $fecha->format('Y-m-d');
    }

    /**
     * Importe del DBF a su unidad mínima como entero: '  1234.50' => 123450 céntimos.
     *
     * Si trae más decimales de los que declara el campo, es un error de datos y se avisa: truncar
     * dinero en silencio es justo el fallo que nadie detecta hasta que no cuadra el cierre del mes.
     */
    public static function aMinimas(string $valor, int $escala = 2): int
    {
        $valor = trim($valor);

        if ($valor === '' || $valor === '.') {
            return 0;
        }

        $negativo = str_starts_with($valor, '-');
        $valor = ltrim($valor, '+-');

        [$entero, $decimales] = array_pad(explode('.', $valor, 2), 2, '');

        if (! ctype_digit($entero.$decimales) && $entero.$decimales !== '') {
            throw new InvalidArgumentException("Importe no numérico en el DBF: '{$valor}'");
        }

        if (strlen($decimales) > $escala) {
            throw new InvalidArgumentException(
                "El importe '{$valor}' trae más de {$escala} decimales; revisa la escala del campo."
            );
        }

        $minimas = (int) ($entero.str_pad($decimales, $escala, '0'));

        return $negativo ? -$minimas : $minimas;
    }

    /** Vuelve a texto decimal para comparar con el sistema antiguo o guardar en un DECIMAL. */
    public static function aDecimal(int $minimas, int $escala = 2): string
    {
        $signo = $minimas < 0 ? '-' : '';
        $digitos = str_pad((string) abs($minimas), $escala + 1, '0', STR_PAD_LEFT);

        if ($escala === 0) {
            return $signo.$digitos;
        }

        return $signo.substr($digitos, 0, -$escala).'.'.substr($digitos, -$escala);
    }
}
