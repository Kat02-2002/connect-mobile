# RoomLink Mobile

Frontend Android implementado dentro del módulo `app` existente. Incluye inicio de sesión real, Dashboard, búsqueda, resultados, confirmación, Mis reservas y detalle de reserva contra AWS. Se utilizaron los seis `screen.png`, `code.html` y `DESIGN.md` de `../prototipo` como referencia visual.

**Validación del 14 de octubre de 2026:** compilación correcta; 16 pruebas unitarias y 3 pruebas instrumentadas aprobadas; Android Lint sin errores; 12 comprobaciones REST contra AWS aprobadas. La aplicación está instalada en `Medium_Phone_API_37.0` y quedó en Login después de verificar el cierre de sesión.

## Ejecutar en Android Studio

1. Abrir esta carpeta, `aplicacionUPC3042`, como proyecto existente.
2. Sincronizar Gradle. Usar el JDK incluido en Android Studio; en este equipo está en `C:\Program Files\Android\Android Studio\jbr`.
3. En SDK Manager, instalar Android API 37 y las herramientas que solicite Gradle. En este equipo el SDK está en `C:\Users\Favio\AppData\Local\Android\Sdk`.
4. Verificar `local.properties` si se mueve el proyecto a otra computadora. Este archivo es local y está excluido de Git.
5. Seleccionar la configuración `app` y un emulador o teléfono Android 11 / API 30 o superior.
6. Pulsar Run. Ingresar las credenciales de un usuario existente; la aplicación no las precarga.

## Ejecutar desde VS Code / PowerShell

Abrir esta carpeta en VS Code y utilizar su terminal PowerShell:

```powershell
Set-Location 'C:\Users\Favio\Documents\rooms\aplicacionUPC3042'
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = 'C:\Users\Favio\AppData\Local\Android\Sdk'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
Start-Process -FilePath "$env:ANDROID_HOME\emulator\emulator.exe" -ArgumentList '-avd','Medium_Phone_API_37.0' -WindowStyle Hidden
```

Cuando el emulador termine de iniciar:

```powershell
.\gradlew.bat :app:installDebug --console=plain
& "$env:ANDROID_HOME\platform-tools\adb.exe" shell am start -n com.jnarvaez.aplicacionupc3042/.MainActivity
```

APK generado: [app-debug.apk](app/build/outputs/apk/debug/app-debug.apk). Es un APK de depuración con firma de desarrollo.

Ejecutar las pruebas de interfaz con el emulador encendido:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --console=plain
```

## Estructura

```text
app/src/main/java/com/jnarvaez/aplicacionupc3042/
├── MainActivity.kt
├── LibroApplication.kt                 Application simple conservada
├── data/
│   ├── model/Models.kt                 DTO y criterios de búsqueda
│   ├── model/BookingRules.kt           Validación y horario de Perú
│   ├── remote/RoomLinkApi.kt           Contratos Retrofit
│   └── repository/RoomLinkRepository.kt
├── presentation/
│   ├── RoomLinkViewModel.kt            StateFlow, sesión y operaciones
│   ├── navigation/AppNavigation.kt    Grafo y Scaffold RoomLink
│   ├── common/RoomLinkComponents.kt    Tarjetas y estados compartidos
│   ├── common/MetaBadge.kt             Componente existente adaptado
│   └── screens/RoomLinkScreens.kt      Seis pantallas y detalle
└── ui/theme/                          Paleta y tipografía Inter
app/src/test/                          Reglas y contratos HTTP
app/src/androidTest/                   Navegación y conflictos
scripts/verify_aws.py                   Verificación REST optativa
docs/qa/                               Evidencias y capturas
```

Arquitectura sencilla: Compose observa `StateFlow` mediante `collectAsStateWithLifecycle`; el ViewModel utiliza coroutines y el repositorio consume Retrofit/OkHttp. La sesión y los filtros viven en el ViewModel de la actividad. Sobreviven a recomposición y rotación; después de que Android termine el proceso, se solicita iniciar sesión nuevamente. La contraseña solo existe como entrada temporal en Login: no se guarda en ViewModel, SavedState, preferencias ni logs.

El esqueleto original era un catálogo académico de libros, con pantallas estáticas y navegación Compose. Sus componentes y pantallas incompatibles permanecen como archivos de referencia, desconectados del grafo que ejecuta RoomLink. No se creó otro proyecto ni se eliminó contenido ajeno al frontend. Se retiró Hilt/KSP de la configuración y las anotaciones de la actividad/Application porque este flujo no requiere inyección de dependencias.

## Configuración y dependencias

| Elemento | Versión / configuración |
|---|---|
| Android Gradle Plugin | 9.3.2, conservado |
| Gradle Wrapper | 9.5.0, conservado |
| Kotlin y plugin de Compose | 2.2.10; Kotlin integrado en AGP |
| JDK utilizado | Android Studio JBR 25.0.3 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 30 |
| Java source / target | 11, conservado |
| Compose BOM / Material 3 | 2026.02.01 / versión gestionada por BOM |
| Navigation Compose | 2.10.0 |
| Lifecycle / ViewModel / StateFlow observado | 2.11.0 |
| Activity Compose | 1.13.0 |
| Retrofit / converter Gson | 3.0.0 |
| OkHttp | 5.5.0, fijado por la dependencia existente |
| Coil Compose | 2.7.0; imágenes remotas y fallback local |
| JUnit / MockWebServer | 4.13.2 / 5.5.0; solo pruebas |
| Instrumentación | AndroidJUnitRunner, Compose UI Test, AndroidX Test |

Coroutines se obtiene de las dependencias AndroidX/Kotlin existentes. La fuente Inter se incluye como recurso local con su licencia en [INTER-LICENSE.txt](INTER-LICENSE.txt). Los cambios de dependencias se organizan en `gradle/libs.versions.toml`.

La configuración mantiene el soporte Kotlin integrado de AGP 9; no se agregó otro plugin Android Kotlin. Referencia: [documentación oficial de Android](https://developer.android.com/build/migrate-to-built-in-kotlin).

## Historias de usuario y endpoints

| Historia | Implementación |
|---|---|
| HU001 | Login real, validación, contraseña visible/oculta, carga y errores |
| HU002 | Saludo, próxima reserva consultada y acceso a búsqueda/historial |
| HU003 | Sede y equipamiento derivados de API; calendario y selector horario nativos; asistentes y validaciones |
| HU004 | Búsqueda filtrada, tarjetas, imágenes/fallback, lista vacía, carga y reintento |
| HU005, HU009 | Revisión, POST de reserva, protección contra doble envío y abandono sin registrar |
| HU006, HU007, HU008, HU010 | Reservas próximas/anteriores, estados según horario de Perú y detalle remoto |

Base URL: `https://ujh0myzrbb.execute-api.us-east-1.amazonaws.com/`.

| Método | Endpoint | Uso |
|---|---|---|
| POST | `/login` | Autenticación y datos de sesión |
| GET | `/salas` | Catálogo y consultas con todos los filtros del contrato |
| GET | `/salas/{id}` | Obtener la sala antes de confirmar |
| POST | `/reservas` | Confirmar con el usuario de la sesión |
| GET | `/reservas?usuarioId={id}` | Dashboard e historial |
| GET | `/reservas/{id}` | Detalle; se verifica que corresponda al usuario |

`POST /usuarios` y `POST /salas` quedan para cargas REST externas, según el alcance solicitado. No se agregaron pantallas administrativas, JWT, Authorization, check-in, cancelación de reservas confirmadas ni modificación de infraestructura AWS.

## Diseño y comportamiento

Paleta azul corporativa, superficies claras, tipografía Inter, tarjetas redondeadas, controles Material 3 y navegación inferior Inicio / Reservar / Mis reservas. Las pantallas desplazan su contenido y respetan insets y teclado. El equipamiento se distribuye en chips que pueden ocupar varias filas.

El prototipo contiene información y acciones sin respaldo en el contrato: bloqueo previo, check-in, contactos de soporte, integrantes, rangos extra de disponibilidad, directorio activo, servidor siempre activo y política de cancelación. Se omitieron esas afirmaciones. Se muestran los valores reales de la API y el horario solicitado. Cuando no hay una imagen utilizable o falla su descarga, se muestra un icono con la etiqueta «Ilustración de sala».

Las fechas y la clasificación usan `America/Lima`. Una reunión confirmada que está en curso permanece en Próximas hasta la hora de fin; después se presenta como Finalizada. El Dashboard y las listas recalculan el tiempo mientras están visibles. Volver conserva filtros; cerrar sesión cancela tareas pendientes, borra datos de sesión y limpia el historial de navegación.

Se manejan HTTP 400, 401, 404, 409 y 5xx, desconexión, timeout y JSON inesperado con mensajes comprensibles. No se muestra contenido de errores técnicos del servidor. La reserva no se reintenta automáticamente: ante un resultado ambiguo se solicita consultar Mis reservas; ante 409 se debe volver a buscar disponibilidad. La confirmación exige una respuesta válida con reserva correspondiente a la solicitud.

## Archivos creados y modificados

**Creados:**

- `data/model/Models.kt`, `data/model/BookingRules.kt`.
- `data/remote/RoomLinkApi.kt`, `data/repository/RoomLinkRepository.kt`.
- `presentation/RoomLinkViewModel.kt`.
- `presentation/common/RoomLinkComponents.kt`.
- `presentation/screens/RoomLinkScreens.kt`.
- `app/src/main/res/font/inter.ttf`, `INTER-LICENSE.txt`.
- `app/src/test/java/com/jnarvaez/aplicacionupc3042/BookingRulesTest.kt` y `RoomLinkRepositoryTest.kt`.
- `app/src/androidTest/java/com/jnarvaez/aplicacionupc3042/RoomLinkFlowTest.kt`.
- `scripts/verify_aws.py`, este README y `docs/qa/` con evidencia REST y capturas.

**Modificados:**

- `MainActivity.kt`, `LibroApplication.kt`.
- `presentation/navigation/AppNavigation.kt` y `presentation/common/MetaBadge.kt`.
- `ui/theme/Theme.kt`, `ui/theme/Type.kt`.
- `app/src/main/AndroidManifest.xml`: permiso Internet.
- `app/src/main/res/values/strings.xml`: nombre RoomLink.
- `app/build.gradle.kts`, `gradle/libs.versions.toml`: dependencias.
- `local.properties`: ruta correcta al SDK de este equipo, con escape de Windows.

No se subieron cambios a GitHub. Esta copia no contiene un repositorio Git inicializado.

## Pruebas y evidencia

| Verificación | Resultado |
|---|---|
| `:app:assembleDebug` | BUILD SUCCESSFUL, APK generado |
| `:app:testDebugUnitTest` | 16 pruebas; 0 fallos |
| `:app:lintDebug` | 0 errores; 14 advertencias de versiones/recursos heredados |
| `:app:connectedDebugAndroidTest` | 3 pruebas; 0 fallos, API 37 / Android 17 |
| Verificación REST AWS | 12 comprobaciones aprobadas, incluidos creación 201 y duplicado 409 |
| Login y consultas desde Android contra AWS | Usuario Demo, Dashboard, detalle, catálogo, búsqueda y Mis reservas comprobados |
| Selectores nativos | Sede, fecha y horario operados en emulador; consulta del horario ocupado devolvió lista vacía |
| Cierre de sesión real | Login sin campos precargados; Atrás no regresó al Dashboard |
| Inspección visual | Siete pantallas revisadas; evidencia en `docs/qa/` |

Las 16 pruebas unitarias incluyen la prueba heredada y 15 nuevas pruebas de fechas, horario en Perú, validación, filtros, contratos, errores HTTP, JSON inesperado, timeout, desconexión y pertenencia de reservas. Las 3 pruebas instrumentadas incluyen la prueba heredada y 2 nuevas pruebas de flujo completo y conflicto, utilizando fixtures exclusivamente en `androidTest`.

Las capturas de [fixtures](docs/qa/fixtures) ilustran todas las pantallas con respuestas de prueba; no representan datos reales de AWS. Las capturas de [AWS](docs/qa/aws) se tomaron con la aplicación de producción conectada a AWS. El POST real y el duplicado secuencial se probaron mediante el script REST; la navegación de creación también se comprobó mediante instrumentación con fixtures.

Informes generados:

- [Pruebas unitarias](app/build/reports/tests/testDebugUnitTest/index.html).
- [Pruebas Android](app/build/reports/androidTests/connected/debug/index.html).
- [Android Lint](app/build/reports/lint-results-debug.html).
- [Resultados REST reales](docs/qa/aws-verification.json).

### Reserva real creada para la verificación

Sala Lima, Oficina Central, piso 3; **15 de octubre de 2026, 16:00–16:30 (Perú)**. Identificador: `0f197081-5977-46b3-bb19-48014d65d494`. Permanece en AWS: la API suministrada no dispone de un endpoint de cancelación. La segunda solicitud idéntica devolvió 409 y no creó otra reserva. Una nueva búsqueda con ese horario excluyó la sala.

### Repetir la verificación REST

Python 3 con biblioteca estándar, sin paquetes adicionales:

```powershell
python scripts/verify_aws.py --email demo@roomlink.com --output docs/qa/aws-readonly.json
```

El script solicita la contraseña de forma interactiva. De forma predeterminada, consulta datos existentes. `--create-test-reservation` habilita una escritura real para mañana, 16:00–16:30; comprueba disponibilidad antes de escribir y no crea otra reserva si ese horario ya está ocupado. Evitar repetirlo para este horario después de la prueba registrada.

## Problemas resueltos y límites de validación

La ruta de SDK original apuntaba a otro usuario de Windows y Java no estaba en PATH; se ajustó `local.properties` y se utilizó el JBR de Android Studio mediante JAVA_HOME. Se corrigió el escape requerido por Lint. Se retiraron las dependencias Hilt/KSP innecesarias. Las pruebas distinguieron el título y el botón que compartían «Confirmar reserva». Tras revisar capturas se ajustó el azul de selección y la jerarquía de encabezados.

Inicialmente `/login` y `/salas` devolvieron HTTP 500; el login informó «Error al acceder a DynamoDB». Posteriormente el servicio respondió correctamente y se completaron las 12 comprobaciones reales. No se modificó el backend para resolver esa incidencia.

No hay funcionalidad pendiente dentro de las seis pantallas solicitadas. No se probaron teléfonos físicos, tabletas, cambios de orientación con POST en curso ni respuestas reales de imagen remota: la sala disponible tiene `imagenUrl` vacío. La clasificación de reservas pasadas se comprobó mediante reglas unitarias; el usuario Demo no tenía reservas anteriores en la API al realizar la prueba. Si una reserva se completa en el servidor pero se pierde su respuesta, consultar Mis reservas antes de volver a reservar.
