<?php

declare(strict_types=1);

/*
| Ejemplo de test HTTP. Laravel 11+ expone GET /up como health check
| (configurado en bootstrap/app.php). Patrón para cualquier endpoint:
|   $this->postJson('/api/recurso', [...])->assertCreated()->assertJsonStructure([...]);
*/

it('responde OK en el health check', function (): void {
    $this->get('/up')->assertOk();
});
