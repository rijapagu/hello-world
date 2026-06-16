# Daily Strength — Configuración de integraciones

## 1. Google Sign-In (Credential Manager)
El login con Google ya está integrado (`GoogleAuthClient`), pero es **opcional**: si no se configura,
el onboarding continúa sin cuenta. Para activarlo:

1. En [Google Cloud Console](https://console.cloud.google.com/) crea credenciales OAuth 2.0:
   - Un **OAuth client ID de tipo Android** (con el package `com.dailystrength` y el SHA-1 de tu
     keystore de firma).
   - Un **OAuth client ID de tipo Web** — su id es el que usa Credential Manager como
     `serverClientId`.
2. Pon el **Web client id** en `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id" translatable="false">TU_WEB_CLIENT_ID.apps.googleusercontent.com</string>
   ```
3. (Opcional) Verifica el `idToken` en tu backend con el Web client id como audiencia.

## 2. AI Coach
1. Despliega el backend de [`/backend`](backend/README.md) con tu `ANTHROPIC_API_KEY`.
2. Apunta la app a su URL en `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "AI_BASE_URL", "\"https://tu-backend.example.com/\"")
   ```
3. La IA es un *enriquecedor*: si el backend no responde o devuelve algo inválido, la app genera el
   workout con su **motor de reglas determinista** (Nunca Cero).

## 3. Samsung Health (Fase 2)
`HealthDataSource` tiene una implementación no-op por defecto. Para datos reales (pasos, sesiones de
tenis/pádel) integra el Samsung Health SDK (requiere aprobación de partner) e inyecta una impl real
en lugar de `NoopHealthDataSource`.

## 4. Avatar 3D / Animaciones (Fase 2)
El avatar usa render 2D vía Coil por defecto. Para el avatar 3D interactivo y las animaciones glTF,
añade SceneView/Filament y provee un `.glb` por `AvatarProvider` / `animationRef`.
