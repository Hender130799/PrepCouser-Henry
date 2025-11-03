# Calistenia y Nutrici?n ? App Android

Proyecto multiplataforma Android nativo (Kotlin + Jetpack Compose) para acompa?ar el progreso en calistenia y el aumento de masa muscular con recetas sencillas en espa?ol latino.

## Documentaci?n

- `docs/product_spec.md`: requisitos funcionales, roadmap y KPIs.
- `docs/architecture.md`: arquitectura t?cnica, m?dulos y decisiones clave.

## Estructura

- `android-app/`: proyecto Gradle multi?m?dulo.
  - `app`: punto de entrada, navegaci?n y configuraci?n de Hilt.
  - `core*`: utilidades de UI, modelos, base de datos y red.
  - `domain`: casos de uso y contratos.
  - `data`: repositorios y m?dulos de inyecci?n.
  - `feature-*`: capas de UI por funcionalidad (onboarding, rutinas, nutrici?n, progreso, ajustes).

## Requisitos de desarrollo

- Android Studio Giraffe o superior.
- JDK 17.
- Gradle 8.7 (ejecutar `./gradlew tasks` tras generar el wrapper).
- Cuenta Firebase para habilitar autenticaci?n, Firestore y Storage.

> El archivo `gradle/wrapper/gradle-wrapper.jar` no se versiona; ejecutar `./gradlew wrapper` para generarlo antes de compilar.

## Pr?ximos pasos sugeridos

- Implementar almacenamiento real en Room/Firestore para repositorios actuales.
- Desarrollar flujos completos de onboarding, rutinas y recetas.
- Definir dise?o visual detallado (tipograf?a, componentes reutilizables).
- Integrar datasets iniciales de ejercicios y recetas con im?genes optimizadas.
