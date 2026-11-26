# DeepSource · Evaluación Parcial 4 (DSY1105)

DeepSource permite organizar proyectos y tareas y registrar las horas que el equipo dedica a cada actividad. La app conserva el detalle, las estadísticas, el temporizador del widget y las notificaciones de confirmación.

## Inicio rápido (Windows / Android Studio)

Para ejecutar el proyecto desde Android Studio, utilizar la variante **debug** en el emulador. No necesita el proxy HTTPS del APK firmado, Docker ni una base de datos externa.

### 1. Abrir el proyecto y verificar Java

Abrir PowerShell. Si el proyecto ya está descargado, entrar a su carpeta:

```powershell
cd E:\DeepSource-LMJJ-ev4 # Ajustar según la ubicación del proyecto
java -version
Test-Path .\backend\projects\build.gradle.kts
Test-Path .\backend\tasks\build.gradle.kts
```

Java debe indicar la versión 17 y ambos `Test-Path` deben devolver `True`. Si aún no se tiene el proyecto, descargar la rama [ev4](https://github.com/Nxcho777/DeepSource-LMJJ/tree/ev4) como ZIP, extraerla y ajustar la ruta anterior, o clonar en una carpeta que no exista:

```powershell
git clone --branch ev4 https://github.com/Nxcho777/DeepSource-LMJJ.git
```

Requisitos: JDK 17, Android SDK 35, Android Studio, emulador o dispositivo API 24 o superior e Internet para dependencias y feriados. Gradle 8.9, AGP 8.7.3 y Kotlin 2.0.21 ya están configurados; no es necesario actualizarlos.

Si PowerShell utiliza otro Java, se puede seleccionar el JDK instalado así (ajustar la ruta según el equipo):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

### 2. Comprobar los puertos

```powershell
Get-NetTCPConnection -LocalPort 8081,8082 -State Listen -ErrorAction SilentlyContinue | Select-Object LocalPort,OwningProcess
```

Si no aparecen procesos, continuar. Si ya hay servicios de esta misma copia funcionando, no iniciarlos de nuevo. Si son de otra versión, cerrar sus terminales con Ctrl+C o identificar primero los PID y detener únicamente los procesos correspondientes. No borrar bases de datos.

### 3. Iniciar los dos microservicios

Desde la raíz, ejecutar este bloque para abrir dos terminales independientes:

```powershell
Start-Process powershell.exe -WorkingDirectory (Get-Location).Path -ArgumentList '-NoExit','-Command','.\gradlew.bat -p backend/projects bootRun'
Start-Process powershell.exe -WorkingDirectory (Get-Location).Path -ArgumentList '-NoExit','-Command','.\gradlew.bat -p backend/tasks bootRun'
```

Alternativamente, abrir dos terminales en la raíz y ejecutar `.\gradlew.bat -p backend/projects bootRun` en una y `.\gradlew.bat -p backend/tasks bootRun` en la otra.

Esperar los mensajes `Started ProjectApplication` y `Started TaskApplication`. Mantener ambas ventanas abiertas mientras se utiliza la aplicación. H2 crea sus archivos de datos automáticamente.

### 4. Verificar el backend

En una tercera terminal:

```powershell
curl.exe -i http://127.0.0.1:8081/api/v1/projects
curl.exe -i http://127.0.0.1:8082/api/v1/tasks
```

Ambas respuestas deben indicar HTTP 200. Una lista `[]` es válida en una base nueva; la aplicación adopta el catálogo local al conectarse por primera vez.

### 5. Ejecutar Android

1. En Android Studio, seleccionar **File > Open** y abrir `E:\DeepSource-LMJJ-ev4` (o la raíz extraída).
2. En **Settings > Build, Execution, Deployment > Build Tools > Gradle**, elegir JDK 17 (JAVA_HOME o Temurin 17) y Distribution: Wrapper.
3. Sincronizar Gradle. Android Studio genera `local.properties` con el SDK instalado.
4. Seleccionar la variante `debug`, el módulo `app` y un emulador encendido.
5. Presionar **Run**. Las URL predeterminadas son `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/`.
6. Iniciar sesión y pulsar **Actualizar catálogo**.

### 6. Cuentas de demostración

| Rol | Correo | Contraseña demo |
|---|---|---|
| Usuario | `marina.soto@deepsource.cl` | `deepsource` |
| Administrador | `admin@deepsource.cl` | `deepsource-admin` |

Son cuentas locales de demostración, no credenciales de producción. La cuenta de administrador permite gestionar el catálogo.

### Solución de problemas

- **Parpadeo o pantalla negra del emulador:** detener el dispositivo virtual y, en Device Manager > Edit > Additional settings, configurar Graphics acceleration: Software y Default boot: Cold. Reiniciar el emulador.

- **Incompatible Gradle JVM version:** seleccionar Java 17 en Gradle JDK; no actualizar Gradle.
- **Sin conexión al catálogo:** verificar ambos HTTP 200, pulsar Actualizar catálogo y comprobar las URL desde Chrome del emulador (`http://10.0.2.2:8081/api/v1/projects` y `http://10.0.2.2:8082/api/v1/tasks`). Si se cambió de backend, utilizar un emulador limpio. Borrar almacenamiento solo en una instalación de demostración sin datos importantes: elimina horas, sesión y Room local.
- **Application Installation Failed (different signature):** debug y release tienen firmas distintas. Usar un emulador separado o desinstalar la versión anterior únicamente si sus datos son prescindibles.
- **Puerto ocupado:** no iniciar una segunda instancia; identificar el proceso antes de detenerlo.
- **Feriados:** requieren conexión a Internet; un fallo externo no impide registrar horas.

Para finalizar, detener los servicios con Ctrl+C en sus respectivas terminales. Los archivos H2 permanecen guardados.

## Configuración técnica de ejecución

Ambos usan Spring Boot, Spring Data JPA y H2 en modo archivo. Escuchan en loopback, en los puertos 8081 y 8082. Cada proceso abre su propio archivo H2. `DATA_DIR` permite indicar la carpeta de datos, `PORT` el puerto del proceso y `BIND_ADDRESS` la interfaz. El servicio de tareas consulta proyectos mediante `PROJECTS_URL` (por defecto, `http://127.0.0.1:8081`). El entorno de ejecución está previsto para uso local o en una red restringida. Las credenciales de H2, si se configuran, entran mediante `DB_USER` y `DB_PASSWORD`.

### Variantes y dispositivos

- **Emulador:** la variante debug usa `10.0.2.2:8081` y `10.0.2.2:8082`.
- **Dispositivo USB:** ejecutar `adb reverse tcp:8081 tcp:8081` y `adb reverse tcp:8082 tcp:8082`. Compilar con `-PDS_DEBUG_PROJECTS_URL=http://127.0.0.1:8081 -PDS_DEBUG_TASKS_URL=http://127.0.0.1:8082`.
- **Release:** configurar `DS_RELEASE_PROJECTS_URL` y `DS_RELEASE_TASKS_URL` con direcciones HTTPS accesibles desde el dispositivo de destino. Se aceptan propiedades Gradle o variables de entorno.
- En debug, la excepción de tráfico HTTP está limitada a loopback y al anfitrión del emulador. Release usa HTTPS.
- El acceso utiliza cuentas locales de demostración. La cuenta de administrador permite realizar operaciones CRUD. Los roles se aplican localmente; los servicios carecen de autenticación remota.
- El perfil permite editar el nombre local y consultar dónde se guardan los datos. Google y la ayuda de contraseña muestran las instrucciones de acceso.

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
- La aplicación Android utiliza un par de servicios y archivos H2 estables. Para cambiar de un entorno de prueba a otro, usar un dispositivo de prueba distinto o un perfil de app limpio. No borrar los datos de una instalación con horas reales.

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

## APK firmado y configuración release

Definir localmente `DS_RELEASE_STORE_FILE`, `DS_RELEASE_STORE_PASSWORD`, `DS_RELEASE_KEY_ALIAS` y `DS_RELEASE_KEY_PASSWORD` junto con las dos URL release. La configuración de Gradle usa esos valores para firmar `assembleRelease`. El APK release fue generado y verificado con firma v2 el 26 de noviembre de 2026, según el resultado de la compilación y verificación de release. Está disponible en [GitHub Releases](https://github.com/Nxcho777/DeepSource-LMJJ/releases), junto con las instrucciones RELEASE.md, el proxy TLS y la evidencia de firma. El APK usa HTTPS localhost:8441 y localhost:8442 mediante un proxy local y ADB reverse; consultar RELEASE.md para ejecutarlo. La variante debug de Android Studio no requiere ese proxy. Verificar con `apksigner verify --verbose --print-certs <apk>` e instalar el APK en el dispositivo de destino. Guardar el keystore y sus credenciales fuera del repositorio público.

## Planificación y colaboración

- [Tablero Trello](https://trello.com/b/Et6gt57B/deepsourceapp)
- [Código fuente EV4](https://github.com/Nxcho777/DeepSource-LMJJ/tree/ev4)

## Integrantes

- José Dolorier
- Jonathan Illanes
- Matías Rojas
- Lucas Saá

