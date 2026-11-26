# DeepSource · Evaluación Parcial 4 (DSY1105)

DeepSource permite organizar proyectos y tareas y registrar las horas que el equipo dedica a cada actividad. La app conserva el detalle, las estadísticas, el temporizador del widget y las notificaciones de confirmación.

## Requisitos

- JDK 17, SDK Android 35 y Android Studio. El proyecto conserva Gradle 8.9, AGP 8.7.3 y Kotlin 2.0.21.
- Emulador Android o dispositivo desde API 24. La primera compilación requiere acceso a los repositorios de dependencias.
- Los comandos siguientes se ejecutan en PowerShell desde la raíz. Android Studio puede crear `local.properties` con la ruta local del SDK.

## Iniciar los servicios

Abrir una terminal por servicio.

```powershell
.\gradlew.bat -p backend/projects bootRun
```

```powershell
.\gradlew.bat -p backend/tasks bootRun
```

Ambos usan Spring Boot, Spring Data JPA y H2 en modo archivo. Escuchan en loopback, en los puertos 8081 y 8082. Cada proceso abre su propio archivo H2. `DATA_DIR` permite indicar la carpeta de datos, `PORT` el puerto del proceso y `BIND_ADDRESS` la interfaz. El servicio de tareas consulta proyectos mediante `PROJECTS_URL` (por defecto, `http://127.0.0.1:8081`). El modo de evaluación es local o en una red restringida. Las credenciales de H2, si se configuran, entran mediante `DB_USER` y `DB_PASSWORD`.

## Ejecutar Android

- **Emulador:** la variante debug usa `10.0.2.2:8081` y `10.0.2.2:8082`.
- **Dispositivo USB:** ejecutar `adb reverse tcp:8081 tcp:8081` y `adb reverse tcp:8082 tcp:8082`. Compilar con `-PDS_DEBUG_PROJECTS_URL=http://127.0.0.1:8081 -PDS_DEBUG_TASKS_URL=http://127.0.0.1:8082`.
- **Release:** configurar `DS_RELEASE_PROJECTS_URL` y `DS_RELEASE_TASKS_URL` con direcciones HTTPS accesibles desde el dispositivo evaluador. Se aceptan propiedades Gradle o variables de entorno.
- En debug, la excepción de tráfico HTTP está limitada a loopback y al anfitrión del emulador. Release usa HTTPS.
- El login de evaluación mantiene las cuentas demo de la app existente. Usar la cuenta de administrador de la entrega para demostrar CRUD. Los roles se aplican localmente; los servicios carecen de autenticación remota.
- El perfil permite editar el nombre local y consultar dónde se guardan los datos. Google y la ayuda de contraseña muestran las instrucciones de acceso de la evaluación.

## Uso y contratos REST

Mantén pulsada una tarjeta de proyecto para abrir estadísticas y acceder a **Editar proyecto** y **Eliminar proyecto** como administrador. En **Registrar tiempo → Seleccionar tarea** están las acciones de crear, editar y eliminar tareas. **Actualizar catálogo** descarga el estado remoto.

| Operación | Proyectos (8081) | Tareas (8082) |
|---|---|---|
| Lista | `GET /api/v1/projects` | `GET /api/v1/projects/{projectId}/tasks` |
| Detalle | `GET /api/v1/projects/{id}` | `GET /api/v1/projects/{projectId}/tasks/{id}` |
| Crear | `POST /api/v1/projects` | `POST /api/v1/projects/{projectId}/tasks` |
| Editar | `PUT /api/v1/projects/{id}` | `PUT /api/v1/projects/{projectId}/tasks/{id}` |
| Eliminar | `DELETE /api/v1/projects/{id}` | `DELETE /api/v1/projects/{projectId}/tasks/{id}` |

Android también usa `GET /api/v1/tasks?includeArchived=true` para leer el catálogo completo de tareas. Los CRUD reciben DTO JSON. Proyecto: `id, name, description, client, totalMinutes, iconIndex, isCompleted`. Tarea: `id, projectId, name, description, estimatedMinutes`. Las respuestas incluyen `archived`. Una creación devuelve 201, una eliminación 204, campos inválidos 400, referencias inexistentes 404 y conflicto de ID o edición de una baja 409. Si el servicio de proyectos está caído, las operaciones de tareas que necesitan validarlo devuelven 503.

La lectura del calendario usa Retrofit y OkHttp para consultar [Nager.Date v4](https://github.com/nager/Nager.Date/blob/main/doc/examples/rest-api.md), ruta `GET /api/v4/Holidays/CL/{year}`. Se muestra un aviso de feriado público nacional en el selector. El aviso es informativo y una caída de la API no bloquea el registro.

## Datos y comportamiento sin conexión

- H2 guarda el catálogo de proyectos y tareas. Room guarda usuarios, horas, progreso local y una copia del catálogo que usa la app y el widget.
- La migración Room 8→9 agrega marcas de baja. El historial conserva IDs y minutos. Al primer contacto se adoptan los catálogos locales, incluido el material de demostración de la instalación inicial, por ID. Un alta repetida lee el elemento remoto existente y conserva sus cambios. La app registra la adopción después de confirmar ambos catálogos.
- Las ediciones del catálogo se confirman primero en el servidor. Con los servicios caídos se puede consultar el catálogo local y registrar horas en los elementos disponibles. Las operaciones de catálogo se pueden volver a intentar cuando vuelve la conexión. No se transfieren horas ni se agregan estadísticas entre dispositivos.
- Eliminar archiva el catálogo y excluye el elemento de nuevas selecciones. Un cronómetro que ya estaba asociado a un elemento conserva sus referencias. El historial de un proyecto eliminado se puede consultar desde la lista de completados.
- El Android de evaluación usa un par de servicios y archivos H2 estables. Para cambiar de un entorno de prueba a otro, usar un dispositivo de prueba distinto o un perfil de app limpio. No borrar los datos de una instalación con horas reales.

## Pruebas

```powershell
.\gradlew.bat -p backend/projects test bootJar
.\gradlew.bat -p backend/tasks test bootJar
.\scripts\Test-BackendPersistence.ps1
.\gradlew.bat :app:ev4UnitCoverage :app:assembleDebug
.\scripts\Measure-AndroidCoverage.ps1
.\gradlew.bat :app:connectedDebugAndroidTest
```

Gradle genera el informe JaCoCo HTML/XML bajo `app/build/reports/coverage/test/debug`. El script muestra el total global y la lógica de arranque, navegación, modelos, datos, ViewModels, notificaciones, animación y widget, con un subtotal que distingue los dos archivos generados por Room. El informe completo conserva esos archivos y las pantallas Compose. La cobertura JVM se reporta separada de las pruebas instrumentadas y backend. Para comprobar ambos servicios desde Android, con los servicios activos y los dos APK de debug instalados, ejecutar:

```powershell
adb shell am instrument -w -e deepSourceLive true cl.duoc.deepsource.test/androidx.test.runner.AndroidJUnitRunner
```

La prueba en vivo crea y archiva registros de demostración en los servicios. Utilizar archivos H2 de pruebas. Para el recorrido manual, comprobar: inicio como usuario y administrador, CRUD del catálogo, horas y estadísticas, conservación del historial, recuperación ante desconexión, feriado visible, widget, temporizador y notificaciones.

### Verificación del 26 de noviembre de 2026

- Compilaron el APK debug, el APK de pruebas y ambos JAR. Pasaron 22 pruebas JVM, 15 pruebas instrumentadas en el emulador y 4 pruebas HTTP del backend.
- JaCoCo midió 1306 de 1499 líneas en las fuentes de lógica escritas en el proyecto (87,12 %), 1970 de 2209 en esos paquetes incluyendo Room generado (89,18 %) y 3378 de 5416 en el informe global (62,37 %).
- Los logs de `build/ev4-android/` registran el CRUD que Android ejecutó contra ambos servicios. La suite confirmó también una consulta Retrofit real de feriados de Chile y el aviso visible en el calendario con una respuesta controlada.
- La prueba `Test-BackendPersistence.ps1` guardó un proyecto y una tarea, reinició ambos procesos y confirmó su lectura desde los mismos archivos H2.

## Firma release y evidencias de la pauta

Definir localmente `DS_RELEASE_STORE_FILE`, `DS_RELEASE_STORE_PASSWORD`, `DS_RELEASE_KEY_ALIAS` y `DS_RELEASE_KEY_PASSWORD` junto con las dos URL release. La configuración de Gradle usa esos valores para firmar `assembleRelease`. Ejecutar la firma final tras revisar las URL y el keystore de entrega. Verificar con `apksigner verify --verbose --print-certs <apk>` e instalar el APK en el dispositivo de evaluación. Guardar el keystore fuera del repositorio y entregarlo por el canal privado indicado por la asignatura.

Adjuntar evidencias por indicador: recorrido de pantallas (3.1.1), persistencia tras reinicio y resultados backend (3.1.2), CRUD desde Android (3.1.3), respuesta Retrofit y aviso de feriado (3.1.4), informe JaCoCo JVM (3.2.1), APK y verificación de firma (3.3.1), y enlaces al historial real de aportes del equipo en GitHub y al tablero Trello con responsables y avances (3.3.2).

## Integrantes

- José Dolorier
- Jonathan Illanes
- Matías Rojas
- Lucas Saá

