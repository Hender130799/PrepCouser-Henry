## Arquitectura T?cnica

### Enfoque General

- **Plataforma**: Android nativo (min SDK 24, target SDK actual).
- **Lenguaje**: Kotlin.
- **Arquitectura**: Clean Architecture + MVVM + Unidirectional Data Flow.
- **UI**: Jetpack Compose con navegaci?n declarativa.
- **Inyecci?n de dependencias**: Hilt.
- **Persistencia local**: Room + DataStore (preferencias).
- **Sincronizaci?n remota**: Firebase (Auth, Firestore, Cloud Storage) y Retrofit para futuras APIs externas.
- **Gesti?n de estados**: ViewModel + Kotlin Flows + coroutines.
- **Testing**: JUnit5, Turbine, MockK, Compose Testing, Espresso para flows cr?ticos.

### M?dulos del Proyecto

1. **app**
   - Configuraci?n de `Application`, navegaci?n global, configuraci?n Hilt.
   - Punto de entrada principal (`MainActivity`).
2. **core**
   - Utilidades comunes (extensiones, formateadores, manejo de errores).
   - Definici?n de estilos, theming Compose.
3. **core-model**
   - Modelos compartidos (entidades de dominio, DTO base).
4. **core-database**
   - Configuraci?n Room, DAOs, entidades locales.
5. **core-network**
   - Definici?n de clientes Retrofit/Firestore, interceptores, DTOs remotos.
6. **domain**
   - Casos de uso, interfaces de repositorio.
7. **data**
   - Implementaciones de repositorios, mapeadores entre capas.
8. **feature-onboarding**
   - Flujo de registro de datos y preferencias iniciales.
9. **feature-workouts**
   - Gesti?n de rutinas, biblioteca de ejercicios, sesi?n en vivo.
10. **feature-nutrition**
    - Cat?logo de recetas, plan de comidas, lista de compras.
11. **feature-progress**
    - Seguimiento de m?tricas, estad?sticas, logros.
12. **feature-settings**
    - Ajustes, perfil, unidades, notificaciones.

Cada m?dulo `feature-*` expone pantallas y `ViewModel`s espec?ficos, comunic?ndose con casos de uso del m?dulo `domain`. El m?dulo `app` orquesta la navegaci?n y ensamblado.

### Capas y Responsabilidades

- **Presentaci?n (UI/Presentation)**: Composables, `ViewModel`s. Manejo de `UiState`, eventos (`UiEvent`), efectos secundarios controlados.
- **Dominio**: Casos de uso puros que orquestan repositorios, encapsulan reglas de negocio (generaci?n de rutinas, c?lculo de macros, progresiones).
- **Datos**: Repositorios que combinan fuentes locales y remotas. Pol?tica Remote-First con fallback offline.

### Gesti?n de Datos

- **Perfil y preferencias**: DataStore Preferences/Proto + Firestore.
- **Rutinas/ejercicios**: Firestore (plan maestro) + Room (descarga offline). Sincronizaci?n dif. utilizando WorkManager.
- **Recetas**: Firestore/Storage para detalles e im?genes. Room para cache.
- **Progreso**: Firestore (historial) + Room (offline). Fotos almacenadas en Cloud Storage con referencias locales.

### Integraciones Clave

- **Firebase Authentication**: Email/password + Google/Facebook. Se usa `FirebaseAuth` con wrappers en `core-network`.
- **Firestore**: Colecciones versionadas (`users`, `workout_plans`, `recipes`, `progress_logs`). Uso de reglas de seguridad.
- **Cloud Storage**: Carpeta por usuario para medias privadas, carpeta p?blica para recursos globales.
- **Push / Recordatorios**: Firebase Cloud Messaging + WorkManager/AlarmManager para disparar notificaciones locales.

### Sincronizaci?n y Offline-first

- Repositorios siguen estrategia `Flow<Resource<T>>`.
- Al iniciar app: WorkManager programa trabajos peri?dicos para sincronizar rutinas, recetas y progreso.
- Conflictos resueltos mediante campos `updatedAt`. ?ltima escritura gana con merge de listas.

### Manejo de Contenido Multimedia

- Uso de Coil para cargar im?genes/GIF WebP.
- Compresi?n previa y transformaciones (blurhash placeholders) para mejorar UX.
- Almacenamiento local en cache interna con pol?tica LRU.

### Seguridad

- Cifrado local: Room con SQLCipher opcional, DataStore cifrado via EncryptedFile.
- Biometr?a: integraci?n `BiometricPrompt` para proteger acceso a vistas sensibles.
- Auditor?a y logging seguro con `Timber` (s?lo debug).

### Pipeline de CI/CD

- GitHub Actions o Firebase App Distribution.
- Lint (detekt, ktlint), tests unitarios/instrumentados en cada PR.
- Generaci?n autom?tica de bundle `*.aab` y despliegue a testers internos.

### Observabilidad

- Firebase Analytics para eventos: onboarding completado, entrenamientos iniciados/completados, recetas guardadas.
- Crashlytics + custom keys para contexto.
- Logging estructurado en `debug` con posibilidad de desactivar en `release`.

### Estrategia de Datos Iniciales

- Seed de ejercicios y recetas embebido en assets JSON. Seeder ejecuta al primer inicio y carga en Room.
- Posteriormente, estos recursos se sincronizan con Firestore para actualizaciones din?micas.

### Diagramas

Se recomienda crear diagramas UML/Arquitectura (no incluidos a?n) usando PlantUML o Excalidraw: componente, flujo de datos, casos de uso clave.
