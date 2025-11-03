## Configuraci?n de Firebase

Para que la app sincronice correctamente con Firestore y utilice las funciones de autenticaci?n/notificaciones, sigue estos pasos:

1. **Crear proyecto**
   - Ingresa a [Firebase Console](https://console.firebase.google.com/) y crea un nuevo proyecto.
   - Habilita Firestore en modo producci?n (elige ubicaci?n cercana a tus usuarios).

2. **Registrar la app Android**
   - En la secci?n *Build > Project Overview > Add app*, elige Android.
   - Usa `com.calisthenia.app` como ID de aplicaci?n (o el que definas en `app/build.gradle.kts`).
   - Descarga el archivo `google-services.json` y col?calo en `android-app/app/google-services.json` (no lo subas al repositorio).

3. **Configurar Firestore**
   - Crea las colecciones esperadas (puedes dejarlas vac?as, la app har? seed inicial):
     - `users` con documento `default`
     - `plans` con documento `default`
     - `recipes`
     - `progress`
   - Ajusta reglas de seguridad. Ejemplo b?sico (reemplaza condiciones por autenticaci?n real):
     ```
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /{document=**} {
           allow read, write: if request.auth != null;
         }
       }
     }
     ```
   - Configura ?ndices si planeas consultas adicionales (por defecto no se necesitan para esta app).

4. **Opcional: Authentication & Cloud Storage**
   - Activa proveedores de autenticaci?n (por ejemplo, Email/Password) si quieres cuentas reales.
   - Si vas a subir im?genes de progreso, habilita Cloud Storage y actualiza las reglas.

5. **Google Services Plugin**
   - El proyecto ya incluye el plugin `com.google.gms.google-services`. Verifica en `app/build.gradle.kts` la l?nea `alias(libs.plugins.google.services)`.

6. **Variables de entorno / CI**
   - Para pipelines, exporta `GOOGLE_SERVICES_JSON` o provee el archivo mediante secretos. Aseg?rate de no versionar credenciales.

7. **Validar**
   - Ejecuta `./gradlew assembleDebug` dentro de `android-app/`.
   - Corre la app y verifica que el perfil, las rutinas generadas, las recetas y el progreso aparecen tanto en la base local como en Firestore.

> Tip: Usa Firebase Emulator Suite para pruebas locales (`firebase emulators:start --only firestore,auth`). Actualiza `FirebaseSources` para apuntar al host `10.0.2.2` en el emulador de Android.
