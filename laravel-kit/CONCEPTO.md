# CONCEPTO — de qué va este kit, por qué es así y en qué punto está

Documento de traspaso. Se lee de arriba abajo y deja a cualquiera —persona nueva en el equipo o
sesión de Claude sin historial— con el cuadro completo: el problema, las decisiones **y su porqué**,
lo que está hecho, lo que falta y lo que aún no se ha decidido.

Los demás documentos cuentan *qué* hay y *cómo* se usa. Este cuenta *por qué*, que es lo que no se
deduce leyendo el código y lo que se pierde en cuanto se cierra la conversación en la que se decidió.

---

## 1. El problema y el objetivo

Se está reescribiendo en Laravel un ERP que hoy corre en **Visual FoxPro 9 sobre MySQL** y que está
en producción facturando todos los días. El sistema antiguo no se apaga el día del estreno: los dos
conviven mientras la migración avanza módulo a módulo.

De ahí salen los dos únicos riesgos que importan:

1. **Perder comportamiento.** Un ERP que calcula un céntimo distinto que ayer no da ningún error:
   simplemente descuadra, y se descubre en el cierre del mes, con el asesor delante.
2. **Pisarse.** Va a haber más de una persona tocando el mismo código (el dueño, el programador
   senior, posiblemente un tercero). Hasta ahora el control era acordarse.

El kit responde a los dos, y a nada más:

- **No pisarse** → ramas cortas, pull request obligatorio y CI que bloquea (`CONTRIBUTING.md`).
- **No perder comportamiento** → tests de paridad contra casos reales del ERP antiguo
  (`MIGRACION-FOXPRO.md` y `tests/Unit/Migration/`).

Lo que el kit **no** es: no es un framework, ni un método propio, ni nada que haya que mantener.
Envuelve herramientas que el ecosistema Laravel ya mantiene y les pone encima las cuatro reglas
propias de una migración desde Visual FoxPro.

---

## 2. Las decisiones y su porqué

Esta es la parte que no está en ningún otro sitio.

### 2.1 GitHub Flow, no git-flow

Una sola rama permanente (`main`), ramas cortas de una semana como mucho, un PR por tarea y
**squash merge**.

*Por qué:* el equipo es de dos o tres personas. La ceremonia de git-flow (`develop`, ramas de
release, hotfix) resuelve problemas de equipos que publican versiones en paralelo; aquí solo añadiría
pasos que la gente se salta. El squash deja la historia de `main` lineal, un commit por PR: revertir
un módulo entero es un `git revert` de un commit, no una arqueología.

### 2.2 El CI bloquea, y la protección de rama se configura en GitHub

Tres checks obligatorios —formato, análisis estático, tests— y un *ruleset* que impide fusionar en
rojo o empujar directamente a la rama principal.

*Por qué:* una regla que depende de que alguien se acuerde de cumplirla no es una regla. Si el CI no
bloquea, el primer día con prisa se fusiona en rojo y a partir de ahí el rojo es el estado normal. La
protección es **configuración en GitHub, no código**: por eso está como checklist en
`CONTRIBUTING.md` §4 y es lo único del kit que hay que hacer a mano una vez.

### 2.3 Pest + Pint + Larastan, nada propio

*Por qué:* son las herramientas que el ecosistema ya mantiene, y ninguna hay que aprenderla dos veces.
Pint viene de serie en las aplicaciones Laravel nuevas; Larastan es PHPStan enseñado a entender
Eloquent y las facades; Pest aporta `dataset()` —que es exactamente la forma de un test de paridad—,
`--parallel` para el CI y `--dirty` para trabajar rápido en local. Pest sustituye a PHPUnit como
dependencia raíz, pero corre encima de él.

### 2.4 Paridad antes que corrección

Si el ERP antiguo redondea de una forma discutible, el código nuevo redondea **igual**, con un test
que lo fija y un comentario que lo explica.

*Por qué (razón práctica, no purista):* si cambias comportamiento y estructura a la vez, el día que
los números no cuadren no sabrás si es un fallo de la migración o la "mejora" que alguien coló. Con
paridad estricta, **cualquier diferencia es un bug** y el diagnóstico es inmediato. Cambiar el
comportamiento sigue siendo posible: pasa a ser una decisión de negocio, con fecha y responsable, no
algo que decide quien está portando el módulo esa tarde.

### 2.5 El dinero, en enteros de la unidad mínima

Se calcula en céntimos con aritmética entera y se guarda en `DECIMAL`. Nunca `float`.

*Por qué:* en coma flotante `2.45 * 0.21` no es `0.5145`, y el ruido se acumula factura a factura
hasta que el libro de IVA no cuadra. La implementación de ejemplo
(`tests/Support/CalculadoraFactura.php`) usa `intdiv()` puro y **no depende de `ext-bcmath`**, que no
estaba disponible en el entorno donde se validó el kit; donde sí lo esté, `bcmath` o `brick/math` son
mejores. La expectativa `toBeImporte()` de `tests/Pest.php` existe para lo mismo: un importe viaja
como cadena decimal, y si alguien lo convierte a `float` por el camino, el test lo caza.

Dentro de esa decisión hay **dos que se copiaron del sistema antiguo en vez de volver a elegirlas**,
y por eso están fijadas con un test propio:

- el IVA se calcula y redondea **por línea** y luego se suma (sobre la base total sale un céntimo
  distinto);
- el redondeo es **half up alejándose del cero** (0,525 → 0,53), como `ROUND()` de VFP, no el
  redondeo bancario.

### 2.6 El fixture JSON es la memoria del sistema viejo

Los casos de `tests/Fixtures/legacy/<modulo>.json` son facturas reales exportadas del ERP antiguo
**con su resultado ya calculado**.

*Por qué:* nadie escribió nunca las reglas de ese ERP; están en el código de VFP y en la cabeza de
quien lo mantuvo veinte años. Cada caso raro que se añade al fixture —abonos, descuentos, varios
tipos de IVA, negativos, redondeos justo en el medio céntimo— es una regla rescatada. Por eso, cuando
aparezca un descuadre en producción, el orden es: **primero se convierte en un caso del fixture,
después se arregla**.

### 2.7 Cobertura 50 y Larastan nivel 5 son puntos de partida, no metas

*Por qué:* son los números con los que un proyecto arranca sin mentir. Un umbral alto de entrada solo
enseña al equipo a saltarse el CI. Se calibran en el primer PR mirando lo que imprime Pest, y a
partir de ahí **solo suben**. Lo mismo con la línea base de Larastan en código legado: solo puede
encoger.

### 2.8 El script compuesto se llama `qa`, no `check`

*Por qué:* Composer reserva `check` para `check-platform-reqs`, así que `composer check` nunca
ejecutaba el pipeline: fallaba en silencio pareciendo que iba bien. Se descubrió validando el kit
sobre un Laravel de verdad, y es el motivo de que la validación end-to-end esté en el método y no
como paso opcional.

### 2.9 El `.gitattributes` que fuerza LF no es cosmético

*Por qué:* el kit se instala en Windows, donde git reescribe los finales de línea a CRLF al hacer
commit. Los hooks son scripts de bash: en cuanto alguien vuelve a clonar el repositorio se encuentra
con `bad interpreter: /usr/bin/env bash^M`, que parece que el kit está roto y cuesta media mañana.
`install.sh` no adivina si tu `.gitattributes` ya lo cubre: **se lo pregunta a git** (`check-attr
eol`), porque un proyecto Laravel nuevo ya trae `* text=auto eol=lf` y avisar ahí sobraría.

---

## 3. Mapa del kit

| Si quieres… | Ve a |
|---|---|
| Entender el porqué y el estado (esto) | `CONCEPTO.md` |
| Instalarlo, o saber qué contiene fichero a fichero | `README.md` |
| Abrir un PR, nombrar una rama, arreglar un CI en rojo | `CONTRIBUTING.md` |
| Portar un módulo o escribir un importador de DBF | `MIGRACION-FOXPRO.md` |
| Copiar el patrón de test de paridad | `tests/Unit/Migration/FacturasParityTest.php` |
| Ver las cuatro trampas del DBF ya cubiertas | `tests/Unit/Migration/NormalizacionDbfTest.php` |
| Que Claude Code trabaje solo con estas reglas | `CLAUDE.md` (se carga solo al abrir el proyecto) |

`tests/Support/` son **implementaciones de ejemplo**: existen para que la suite arranque en verde y
se sustituyen por las clases reales de `App\` al migrar cada módulo. Lo que se conserva son los tests.

---

## 4. Estado

### Hecho y verificado (septiembre de 2026)

Sobre un `composer create-project laravel/laravel` recién creado —Laravel 13, PHP 8.4, Pest 5.1.3 +
pest-plugin-laravel 5.0.1, Larastan 3.11, Pint 1.30.5—:

- `composer qa` en verde con los 27 tests del kit.
- `install.sh` idempotente: la segunda ejecución no toca nada.
- El hook `pre-commit` rechaza un fichero mal formateado y lo acepta tras pasar Pint.
- Los tests de paridad **se comprobaron rompiendo el cálculo del IVA a propósito**: fallan con la
  diferencia de un céntimo esperada. Un test de paridad que no se ha visto fallar no prueba nada.
- Los tres casos de `.gitattributes` (ya fuerza LF · no lo fuerza · no existe).

### Pendiente, del propio kit

- Calibrar `MIN_COVERAGE` en el primer PR real (hoy está en 50, que es un número de arranque).
- Subir el nivel de Larastan de 5 en adelante, de uno en uno, cuando el CI lleve tiempo en verde.
- Crear el *ruleset* de la rama protegida en GitHub: lo hace el propietario del repositorio, una vez.
- Sustituir `tests/Support/` por las clases reales conforme se migre cada módulo.

### El proyecto de destino, tal y como está hoy

El Laravel al que va el kit (`softec-laravel`) ya existe y tiene historia propia: Laravel 11.31,
Livewire 4, Dusk, **PHPUnit 11 con 87 ficheros de test** (10 en `Unit`, 68 en `Feature`, 9 de Dusk), Pint ya presente, y todo corriendo en Docker
(imagen `php:8.4-fpm-alpine` con Composer 2 y `bcmath` dentro, nginx y MariaDB 11.8). Git local en la
rama **`master`** y **sin remoto configurado**.

Eso deja seis cosas que resolver antes de instalar. Ninguna es un defecto del kit: es el encaje.

1. 🔴 **Los tests apuntan a la base real con el schema legado.** En `phpunit.xml` las líneas de
   sqlite en memoria están comentadas, así que la suite usa la conexión de `.env` —la MariaDB de
   desarrollo— y 67 de los 68 tests de `Feature` no usan `RefreshDatabase`. El kit lo aplicaba a todo
   `Feature`: habría ejecutado `migrate:fresh` y **borrado todas las tablas**, incluido el schema legado
   que ninguna migración de Laravel reconstruye. Corregido en el kit —`tests/Pest.php` lo trae comentado,
   como un Laravel nuevo—, pero al instalar hay que confirmar que sigue apagado.
2. **Sin remoto en GitHub no hay PR ni CI.** Subir el repositorio es el primer paso; hasta entonces,
   del kit solo funcionan los hooks locales y `composer qa`.
3. **La rama principal se llama `master`** y el kit apunta a `main` (el workflow, el ruleset y los
   ejemplos de `CONTRIBUTING.md`). Hay que renombrar la rama o adaptar esas tres referencias.
4. **`composer.json` declara `php ^8.2`.** El contenedor ya corre 8.4, así que el problema no es el
   runtime sino el suelo declarado: con `^8.2`, Composer no resolverá la versión de Pest que el kit
   espera. Hay que subirlo a `^8.3` o `^8.4` antes de instalar.
5. **PHP y Composer viven dentro del contenedor**, no en el Windows anfitrión. `install.sh` los
   necesita (incluso con `--skip-composer`, porque el paso que fusiona los scripts de `composer.json`
   usa `php -r`), así que lo natural es ejecutarlo **dentro del contenedor `app`**, donde ya hay bash,
   git, PHP 8.4 y Composer 2.
6. **Los 87 tests existentes están escritos como clases de PHPUnit** y el kit quita `phpunit/phpunit`
   como dependencia raíz para poner Pest. Pest corre sobre PHPUnit y normalmente los ejecuta sin
   tocarlos, pero eso **hay que verlo en verde en una rama antes de fusionar**, no darlo por hecho.

Y dos avisos:

- El proyecto **ya tiene su propio `CLAUDE.md`** (el contrato del ejecutor de la migración). El del
  kit llegará como `CLAUDE.md.kit` y hay que fusionar a mano: lo que aporta es la sección de método
  (invariantes, comandos, convenciones), no sustituir lo que ya hay.
- La V2 web está **en pausa por decisión del dueño** hasta cerrar la migración 1:1 en Visual FoxPro.
  Instalar el kit no reanuda nada: deja el terreno preparado para cuando se reanude.

---

## 5. Cómo arrancar la sesión nueva

1. **Traer el kit** (el repositorio es público, no hace falta autenticarse):

   ```bash
   git clone --depth 1 --branch claude/git-workflow-php-tests-a33too \
     https://github.com/rijapagu/hello-world.git kit
   ```

   Sin git: *Code → Download ZIP* eligiendo esa rama. De todo lo que baja solo interesa
   `laravel-kit/`; el resto del repositorio es una aplicación Android sin relación.

2. **Leer, en este orden:** este documento → `CONTRIBUTING.md` → `MIGRACION-FOXPRO.md`.

3. **Resolver los seis puntos del §4** antes de ejecutar el instalador. Conviene hacerlo en una rama
   (`chore/laravel-kit`), no sobre la principal.

4. **Instalar** desde Git Bash o dentro del contenedor:

   ```bash
   bash laravel-kit/install.sh /ruta/al/proyecto-laravel
   cd /ruta/al/proyecto-laravel && composer qa
   ```

5. A partir de ese momento, **cualquier sesión de Claude Code abierta en la raíz del proyecto carga
   sola el `CLAUDE.md`** y trabaja con estas reglas sin que haya que pegar nada.

### Texto listo para pegar como primer mensaje

Mientras el kit no esté instalado (y por tanto no exista el `CLAUDE.md` en el proyecto), esto le da a
una sesión nueva todo el contexto de golpe:

```
Estoy migrando un ERP de Visual FoxPro 9 + MySQL a Laravel. El proyecto Laravel está
en <ruta del proyecto> y corre en Docker.

Uso el "laravel-kit": rama claude/git-workflow-php-tests-a33too del repositorio
público github.com/rijapagu/hello-world, carpeta laravel-kit/. Trae el workflow de
git, la CI y los patrones de test de la migración.

Lee laravel-kit/CONCEPTO.md entero antes de tocar nada (ahí está el porqué de cada
decisión y el estado real), y después CONTRIBUTING.md y MIGRACION-FOXPRO.md.

Reglas que no se negocian: paridad antes que corrección (el código nuevo replica el
comportamiento del ERP viejo, incluidos sus redondeos), el dinero en enteros de
céntimo y nunca en float, cada cambio lleva su test, y un test no se desactiva ni se
salta para poner algo en verde.

Empieza diciéndome qué hace falta para instalar el kit aquí, sin instalarlo todavía.
```

---

## 6. Qué NO está decidido

- **El workflow es una propuesta.** `CONTRIBUTING.md` arranca con un aviso `TODO` a propósito: hay
  que alinearlo con lo que se acuerde en la sesión de "método de trabajo con git" y borrar el aviso.
  Si el equipo prefiere otra cosa, se cambia; lo que no se negocia es que haya *una* forma escrita.
- **Cómo se extraen los datos del DBF.** Depende de si Visual FoxPro sigue disponible en la empresa:
  si lo está, exportar desde el propio VFP es la vía más fiable, porque conoce sus tipos.
  `MIGRACION-FOXPRO.md` §3 lista las cuatro vías con lo que cuesta cada una. Para los módulos cuyos
  datos ya viven en MySQL, esa parte no aplica; **el test de paridad sí**.
- **`master` o `main`**: renombrar la rama del proyecto o adaptar el kit (§4, punto 3).
- **Idioma de los commits.** Propuesta: tipo en inglés, resumen en español. Sin cerrar.
- **Quién revisa los PR y con cuántas aprobaciones.** Propuesta: una revisión de otra persona. Con
  dos programadores es viable; conviene confirmarlo antes de activar el ruleset.

---

*Este documento es la fuente del contexto: cuando una de estas decisiones cambie, se actualiza aquí
—con el porqué— antes que en ningún otro sitio.*
