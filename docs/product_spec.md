## Visi?n General

Aplicaci?n Android nativa en espa?ol latino orientada a deportistas de calistenia y usuarios que buscan ganar masa muscular mediante entrenamiento funcional y nutrici?n personalizada. Combina rutinas autom?ticas y personalizadas, seguimiento de progreso detallado y recetas sencillas, ricas en calor?as y nutrientes.

## Objetivos de Usuario

- Registrar m?tricas corporales iniciales y actualizarlas f?cilmente.
- Obtener planes de entrenamiento de calistenia ajustados a nivel, objetivos y disponibilidad de tiempo.
- Crear y modificar rutinas manualmente con biblioteca de ejercicios.
- Acceder a recetas hipercal?ricas sencillas, con ingredientes accesibles y pasos claros.
- Llevar un registro visual del progreso (peso, medidas, rendimiento) y recibir recordatorios motivacionales.
- Operar totalmente en espa?ol latino, con tono cercano y motivador.

## Perfiles de Usuario

1. **Principiante**: poco o nulo conocimiento en calistenia; requiere gu?as paso a paso y progresiones.
2. **Intermedio/Avanzado**: experiencia previa; busca rutinas estructuradas y seguimiento de rendimiento.
3. **Usuario nutricional**: enfocado en recetas y planes alimentarios para ganar masa muscular.

## Flujo Principal

1. **Onboarding y registro**
   - Registro mediante correo/Google/Facebook.
   - Cuestionario inicial: peso, altura, edad, g?nero, nivel de experiencia, objetivos (hipertrofia, fuerza, perder grasa), disponibilidad semanal, restricciones o preferencias alimenticias.
   - Selecci?n de unidades (kg/cm o lb/in), editable posteriormente.
   - Configuraci?n de recordatorios (entrenamiento, comidas, hidrataci?n).

2. **Dashboard / Inicio**
   - Resumen de progreso (peso actual, ?ltimos entrenamientos, racha activa).
   - CTA r?pido para iniciar entrenamiento o revisar receta del d?a.
   - Mensaje motivacional personalizado.

3. **Rutinas**
   - **Generaci?n autom?tica**: Algoritmo que crea plan semanal seg?n perfil, nivel y objetivo. Incluye calentamiento, circuito principal y enfriamiento.
   - **Rutinas manuales**: Creador con arrastrar y soltar ejercicios, configuraci?n de series, repeticiones/tiempo, descanso, notas.
   - **Biblioteca de ejercicios**: filtros por grupo muscular, dificultad, equipamiento. Cada ejercicio con descripci?n, t?cnica, tips y animaci?n/GIF.
   - **Sesi?n en vivo**: cron?metro, repeticiones asistidas, opci?n de marcar ejercicio como completado, registrar RPE y notas.
   - **Historial**: listado de sesiones pasadas, m?tricas de rendimiento (time under tension, reps totales, volumen estimado).

4. **Nutrici?n y Recetas**
   - Cat?logo de recetas hipercal?ricas con filtros (tiempo, presupuesto, tipo: desayuno/almuerzo/cena/snack).
   - Fichas con foto/ilustraci?n, ingredientes, pasos, tiempo estimado, porciones, macros (prote?nas, carbohidratos, grasas, calor?as), tips de preparaci?n.
   - Plan semanal sugerido, integrable con rutinas (ej. recetas para los d?as de entrenamiento pesado).
   - Lista de compras generada autom?ticamente.

5. **Seguimiento y Estad?sticas**
   - Registro manual o sincronizado de peso, medidas corporales, fotos de progreso.
   - Gr?ficos interactivos (peso, porcentaje de grasa estimado, volumen de entrenamiento, PRs).
   - Logros, insignias por consistencia y metas alcanzadas.
   - Exportaci?n de datos (PDF/CSV) compartible.

6. **Comunidad (fase futura opcional)**
   - Feed interno con retos, progresos destacados y comentarios.
   - Retos semanales/mensuales, ranking amistoso.

## Requisitos Funcionales Detallados

### Autenticaci?n y perfiles
- Registro/inicio de sesi?n con email y proveedores sociales.
- Edici?n de perfil: foto, alias, datos biom?tricos, preferencias.
- Gesti?n de varias m?tricas por usuario (peso hist?rico, medidas, fotos).
- Seguridad: verificaci?n email, recuperaci?n de contrase?a.

### Motor de rutinas
- Algoritmo basado en reglas y progresiones (niveles Principiante/Intermedio/Avanzado).
- Ajuste autom?tico de volumen (series, repeticiones, tiempo bajo tensi?n) seg?n progreso.
- Capacidad de pausar o reprogramar rutinas.
- Integraci?n con temporizador y recordatorios.

### Biblioteca de ejercicios
- Almacenar metadatos: nombre, descripci?n, m?sculos primarios/secundarios, equipamiento, nivel, demostraciones multimedia.
- Favoritos y ejercicios personalizados.

### Gestor de recetas
- Base alimentaria con categor?as y etiquetas (alto en prote?na, vegetariana, etc.).
- C?lculo autom?tico de macros por porci?n.
- Sugerencias seg?n objetivo cal?rico diario del usuario.
- Generador de plan semanal y lista de compras.

### Seguimiento
- Registro de m?tricas con fecha/hora, notas.
- Gr?ficos hist?ricos + comparativas entre periodos.
- Recordatorios configurables (push notifications).
- Exportaci?n y respaldo en la nube.

## Requisitos No Funcionales

- Idioma: espa?ol latino, con tono motivador y cercano.
- Rendimiento: tiempos de carga < 2s en vistas principales bajo red 4G.
- Offline-first: rutinas, recetas y progreso accesibles sin conexi?n; sincronizaci?n diferida.
- Accesibilidad: compatibilidad con TalkBack, tama?os de fuente ajustables, alto contraste.
- Seguridad: cifrado local (EncryptedSharedPreferences/SQLCipher), TLS para comunicaciones.
- Escalabilidad: backend soporta 100k usuarios activos con m?nima degradaci?n.

## Integraciones y Contenido

- **Backend:** Firebase Authentication, Firestore para datos, Cloud Storage para multimedia.
- **Anal?tica:** Firebase Analytics, Crashlytics.
- **Push:** Firebase Cloud Messaging.
- **Multimedia:** Almacenamiento de im?genes/GIF optimizados en CDN.
- **Datos iniciales:** cat?logo curado de ejercicios y recetas con derechos libres.

## Roadmap Fases

1. **MVP (12 semanas)**
   - Onboarding b?sico, generaci?n de rutinas autom?ticas, historial simple.
   - Biblioteca de ejercicios con im?genes est?ticas.
   - Cat?logo de recetas con macros y lista de compras b?sica.
   - Registro de m?tricas corporales y gr?ficos esenciales.

2. **Iteraci?n 2**
   - Creador de rutinas manual, animaciones/GIF, recetas con videos cortos.
   - Recordatorios avanzados, badges, exportaci?n PDF.

3. **Iteraci?n 3**
   - Comunidad, retos, recomendaciones de IA, contenido premium.

## KPIs

- Activaci?n onboarding completado > 70% usuarios registrados.
- Retenci?n semanal > 40% en usuarios activos.
- Promedio de sesiones de entrenamiento completadas/usuario/semana.
- Promedio de recetas guardadas y preparadas.
- NPS y feedback cualitativo.

## Riesgos y Mitigaciones

- **Adopci?n de contenido visual**: asegurar licencias y optimizaci?n; usar compresi?n WebP/AVIF.
- **Retenci?n**: implementar gamificaci?n, notificaciones personalizadas y contenido frecuente.
- **Exactitud nutricional**: validar macros con nutricionistas, avisos legales claros.
- **Privacidad**: cumplir con pol?ticas de Google Play y regulaciones locales (ej. Ley Federal de Protecci?n de Datos en M?xico).

## Glosario

- **RPE**: Tasa de esfuerzo percibido.
- **PR**: Personal Record.
- **MVP**: Producto m?nimo viable.
