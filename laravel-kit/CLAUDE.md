# CLAUDE.md — cómo se trabaja en este proyecto

Este fichero lo carga sola cualquier sesión de Claude Code abierta en la raíz del proyecto.
Es el resumen operativo; el porqué de cada decisión está en [`CONCEPTO.md`](CONCEPTO.md).

## Qué es esto

Reescritura en Laravel de un ERP que hoy corre en Visual FoxPro 9 sobre MySQL. El sistema
antiguo sigue en producción mientras se migra módulo a módulo, así que los dos conviven y
tienen que dar los mismos números.

## Invariantes (no se negocian)

1. **Paridad antes que corrección.** El código nuevo replica el comportamiento del ERP
   antiguo, incluidos sus redondeos discutibles, con un test que lo fija y un comentario que
   lo explica. Cambiar comportamiento es una decisión de negocio con fecha y responsable,
   nunca un arreglo que se cuela al portar un módulo.
2. **El dinero va en enteros de la unidad mínima** (céntimos) y se guarda en `DECIMAL`.
   Nunca `float` ni `DOUBLE`, en ningún punto del recorrido: `2.45 * 0.21` en coma flotante
   no es `0.5145`, y ese ruido se acumula hasta descuadrar el libro de IVA.
3. **Cada cambio lleva su test.** Es la *Definition of Done*. Si el cambio no se puede
   probar, primero se hace comprobable. La tabla de qué test exige cada tipo de cambio está
   en [`CONTRIBUTING.md`](CONTRIBUTING.md) §3.
4. **Un test no se desactiva ni se salta para poner algo en verde.** Ni con `skip`, ni
   bajando el mínimo de cobertura, ni fusionando el CI en rojo "porque es un flake". Si es un
   flake, se arregla el test.
5. **Nada apunta a la base de datos de producción**: ni un test, ni un script, ni una prueba
   manual.

## Comandos

```bash
composer qa          # lint + análisis estático + tests (lo que exige el CI)
composer lint:fix    # Pint arregla el formato solo
composer test:dirty  # Pest solo sobre lo modificado (rápido, mientras desarrollas)
composer hooks:install
```

## Convenciones

- Ramas cortas desde la rama principal, una rama = una tarea = un PR:
  `migrate/<modulo>`, `feat/<tema>`, `fix/<tema>`, `chore/<tema>`.
- Commits en formato Conventional Commits: `feat(facturas): calcular IVA por línea`.
- A la rama principal se llega **solo por PR con el CI en verde** y una revisión aprobada.
- Identificadores en inglés; captions y mensajes al usuario en español.

## Dónde mirar

| Necesito… | Fichero |
|---|---|
| Entender el porqué de todo esto y en qué punto está | `CONCEPTO.md` |
| Abrir un PR, nombrar una rama, resolver un fallo del CI | `CONTRIBUTING.md` |
| Portar un módulo, importar un DBF, evitar las trampas del legado | `MIGRACION-FOXPRO.md` |
| El patrón de test de paridad y el de normalización | `tests/Unit/Migration/` |

⚠️ Las clases de `tests/Support/` son **implementaciones de ejemplo** que solo existen para
que la suite arranque en verde. Al migrar cada módulo se sustituyen por las clases reales de
`App\`; lo que hay que conservar son los tests, no ellas.
