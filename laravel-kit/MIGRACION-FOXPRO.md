# Migrar el ERP de Visual FoxPro a Laravel

Complemento de [`CONTRIBUTING.md`](CONTRIBUTING.md). Aquel dice *cómo trabajamos*; este dice *qué tiene
de particular migrar un ERP de VFP* y qué debe cubrir la suite de tests para que la migración no se
descubra rota en el cierre del mes.

## 1. La regla que ordena todo lo demás

**Primero se replica el comportamiento, después se mejora.** Si el ERP antiguo redondea el IVA de una
forma discutible, el código nuevo redondea igual, con un test que lo fija y un comentario que lo explica.
Cambiar ese comportamiento es una decisión de negocio con fecha y responsable, nunca un arreglo que un
programador cuela mientras porta un módulo.

El motivo es práctico: si cambias comportamiento y estructura a la vez, cuando los números no cuadren no
sabrás si es un fallo de la migración o la "mejora". Con paridad estricta, cualquier diferencia es un bug.

## 2. Estrategia: módulo a módulo, con los dos sistemas conviviendo

Migrar un ERP de golpe es la forma habitual de no migrarlo nunca. El patrón que funciona es sustituir
piezas mientras el sistema antiguo sigue en producción:

1. **Informes primero (solo lectura).** Riesgo casi nulo y te obliga a resolver ya la extracción de datos
   y la codificación. Si un listado nuevo cuadra con el del ERP viejo, la base está bien.
2. **Datos maestros después** (clientes, artículos, proveedores, tarifas). Cambian poco y son la base de
   todo lo demás.
3. **Operativa transaccional** (pedidos, albaranes, facturas). Aquí es donde vive la lógica de negocio de
   verdad y donde los tests de paridad valen su peso en oro.
4. **Contabilidad y cierres, al final.** Es lo que más duele si falla y lo que más depende del resto.

Durante la convivencia, **reconcilia a diario**: un proceso que compara totales de los dos sistemas
(ventas del día, saldo de un cliente, existencias de un almacén) y avisa de cualquier diferencia. Es el
único modo de enterarte de un fallo en horas y no en meses.

## 3. Sacar los datos del DBF

Las opciones, de más a menos recomendable según tu caso:

| Vía | Cuándo usarla | Aviso |
|---|---|---|
| Exportar a CSV/SQL desde el propio VFP | Si aún tienes VFP funcionando | Es la más fiable: VFP conoce sus tipos. Cuida la codificación al exportar. |
| Conversor comercial DBF→MySQL | Carga inicial grande y con prisa | Revisa a mano tipos y decimales; suelen convertir importes a float. |
| Librería PHP (`inok/dbf`, `hisamu/php-xbase`, `totalcrm/php-dbf`) | Si necesitas importar repetidamente desde PHP | Comprueba que la tuya lee campos memo (`.fpt`) y la marca de borrado. |
| `VFPOLEDB` por ODBC | Windows | El proveedor es de 32 bits: no lo verás desde un PHP de 64 bits sin un puente. |

La extensión `dbase` de PHP no vale para tablas de Visual FoxPro modernas: solo maneja formatos
antiguos, y ya no forma parte del núcleo del lenguaje.

Recomendación: **importación repetible y automatizada**, no un volcado manual de una tarde. La vas a
ejecutar decenas de veces mientras desarrollas, y el día del cambio la ejecutas una más.

## 4. Las cuatro trampas que corrompen datos en silencio

Están cubiertas por `tests/Unit/Migration/NormalizacionDbfTest.php`. Ninguna da error al importar: el
proceso termina bien y los datos quedan mal.

**Registros borrados.** VFP no borra, marca el registro con un asterisco y lo deja en el fichero. Si el
importador lee todo, resucitas facturas anuladas. Decide explícitamente qué haces con ellos y tenlo en un
test. (Ojo: si alguien hizo `PACK` sobre la tabla, esos registros ya no existen y no hay vuelta atrás.)

**Codificación.** Los DBF creados en DOS suelen ser CP850 y los tocados desde Windows, CP1252. El código
de página del cabecero **puede estar vacío o ser mentira**. El mismo byte es `Ñ` en una y `¥` en otra:
equivocarse no da error, simplemente borra las eñes de toda la base de datos. Verifícalo contra nombres
reales con eñes y acentos antes de la carga masiva.

**Fechas.** La fecha vacía de VFP no es NULL, son espacios o ceros. Sin normalizar acaba como
`0000-00-00`. Y en datos de veinte años hay fechas imposibles: hay que rechazarlas, no dejar que PHP las
"arregle" convirtiendo el 31 de septiembre en 1 de octubre.

**Importes.** Nunca `float`. En coma flotante `2.45 * 0.21` no es `0.5145`, y ese ruido se acumula. Se
trabaja **en enteros de la unidad mínima** (céntimos) y se guarda en columnas `DECIMAL`, nunca `FLOAT` ni
`DOUBLE`. Si tu proyecto puede instalar `ext-bcmath` o `brick/math`, mejor todavía.

Añade una quinta si tu ERP la tiene: **integridad referencial**. VFP casi nunca la impone, así que es
normal encontrar líneas cuya factura no existe. El importador debe **contarlas y reportarlas**, no
descartarlas en silencio ni reventar.

## 5. El test de paridad, módulo a módulo

Es el patrón central, en `tests/Unit/Migration/FacturasParityTest.php`:

1. Del ERP antiguo exportas **casos reales con su resultado ya calculado** a
   `tests/Fixtures/legacy/<modulo>.json`.
2. El test recorre esos casos y exige que el código nuevo dé exactamente lo mismo.
3. Los casos raros valen más que los normales: abonos, descuentos, varios tipos de IVA, cantidades
   decimales, importes negativos, redondeos justo en el medio céntimo.

El ejemplo incluido fija dos decisiones que hay que **copiar** del sistema antiguo, no volver a elegir:
el IVA se redondea por línea y luego se suma (sobre la base total sale un céntimo distinto), y el
redondeo es *half up* alejándose del cero, como `ROUND()` de VFP, no el bancario.

Cuando aparezca un descuadre en producción, conviértelo en un caso más del fixture antes de arreglarlo.
El fixture acaba siendo la memoria de todo lo que el ERP antiguo hacía y nadie había escrito nunca.

## 6. Los tests contra el schema legado

Durante la convivencia, tus tests de `Feature` probablemente apunten a la base de datos real con el
schema del sistema antiguo: es justo lo que hace que prueben algo. Eso trae una consecuencia que hay
que tener clara desde el primer día.

**No actives `RefreshDatabase` sobre esa base.** El trait ejecuta `migrate:fresh`, que borra todas las
tablas antes de aplicar las migraciones — y las migraciones de Laravel solo crean lo que has añadido
tú, nunca el schema legado, que viene de un volcado del sistema antiguo. El resultado es una base
vacía y una tarde de restauración. Por eso `tests/Pest.php` lo trae comentado.

Las tres salidas, de menos a más trabajo:

| Salida | Cuándo |
|---|---|
| Tests de `Unit`, sin base de datos | Toda la lógica de cálculo, incluida la de paridad. Es donde debería vivir la mayor parte. |
| Una base de test aparte, recreable desde el volcado | Cuando el test necesita datos reales. La recrea un script, no `migrate:fresh`. |
| `RefreshDatabase` solo en los tests que lo declaran | Para lo que sí es tuyo —tablas nuevas, autenticación— y se puede reconstruir con migraciones. |

Y comprueba dónde apunta `phpunit.xml`: si trae comentadas las líneas de sqlite en memoria, tus tests
están usando la base de `.env`, que suele ser la de desarrollo.

## 7. Checklist por módulo migrado

Antes de abrir el pull request de un módulo:

- [ ] Fixture con casos reales del ERP antiguo y test de paridad en verde.
- [ ] Casos límite incluidos: negativos, cero, descuentos, varios tipos de IVA, fechas vacías.
- [ ] La importación del módulo es repetible y se puede volver a lanzar sin duplicar datos.
- [ ] Importes en `DECIMAL`, nunca en coma flotante, de punta a punta.
- [ ] Registros borrados y huérfanos: decisión tomada, contada y con test.
- [ ] Reconciliación: existe una consulta que compara este módulo entre los dos sistemas.
- [ ] Documentado en el propio código cualquier comportamiento raro que se ha replicado a propósito.
