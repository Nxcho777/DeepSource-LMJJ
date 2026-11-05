# DeepSource

Aplicación móvil desarrollada en Android para gestionar proyectos y registrar las horas de trabajo dedicadas a cada uno. Permite organizar tareas, consultar el avance de los proyectos y mantener un historial de los registros de tiempo.

## Funcionalidades

- **Gestión de proyectos:** crear proyectos, consultar su información, revisar las horas acumuladas y marcarlos como completados.
- **Registro de horas:** registrar el tiempo trabajado indicando proyecto, integrante, fecha, duración y descripción de la tarea.
- **Validación de formularios:** comprobar los datos ingresados y mostrar mensajes de error cuando corresponda.
- **Persistencia local:** almacenar proyectos, usuarios y registros de horas mediante Room, conservando la información al cerrar la aplicación.
- **Notificaciones:** mostrar una confirmación al registrar horas, según los permisos del dispositivo y las preferencias del usuario.
- **Widget de Android:** consultar un resumen de hasta cuatro proyectos desde la pantalla de inicio y acceder al registro de horas.

La aplicación incluye datos de ejemplo que se cargan durante su primera ejecución.

## Tecnologías utilizadas

- **Kotlin:** lenguaje de programación.
- **Jetpack Compose y Material 3:** desarrollo de interfaces.
- **Navigation Compose:** navegación entre pantallas.
- **MVVM, ViewModel y StateFlow:** arquitectura y gestión de estado.
- **Room y SQLite:** almacenamiento local de datos.
- **Corrutinas y Flow:** operaciones asíncronas y observación de datos.
- **AppWidgetProvider y RemoteViews:** implementación del widget.
- **Android Notifications API:** notificaciones del sistema.
- **Gradle Kotlin DSL:** configuración y compilación del proyecto.

## Estructura del proyecto

```text
app/
├── src/
│   ├── main/
│   │   ├── java/cl/duoc/deepsource/
│   │   │   ├── data/          # Base de datos y repositorio
│   │   │   ├── domain/        # Modelos de datos
│   │   │   ├── navigation/    # Navegación
│   │   │   ├── notification/  # Notificaciones y permisos
│   │   │   ├── ui/            # Pantallas, componentes y ViewModels
│   │   │   └── widget/        # Widget de Android
│   │   ├── res/              # Recursos de la aplicación
│   │   └── AndroidManifest.xml
│   ├── test/                 # Pruebas unitarias
│   └── androidTest/          # Pruebas instrumentadas
└── schemas/                  # Esquemas de Room
```

## Requisitos

- Android Studio compatible con Android Gradle Plugin 8.7.3.
- JDK 17.
- Android SDK 35.
- Emulador o dispositivo con Android 7.0 (API 24) o superior.

El proyecto incluye Gradle Wrapper, por lo que no es necesario instalar Gradle por separado.

## Instalación y ejecución

1. Clonar la rama `ev2` del repositorio:

   ```bash
   git clone --branch ev2 https://github.com/Nxcho777/DeepSource-LMJJ.git
   ```

2. Abrir la carpeta `DeepSource-LMJJ` en Android Studio.
3. Esperar la sincronización de Gradle.
4. Seleccionar un emulador o dispositivo Android.
5. Ejecutar la configuración `app`.

### Acceso de demostración

Para ingresar a la aplicación, utilizar las siguientes credenciales:

```text
Correo: marina.soto@deepsource.cl
Contraseña: deepsource
```

El inicio de sesión utiliza credenciales locales de demostración y no requiere conexión con un servicio de autenticación externo.

## Integrantes

- José Dolorier
- Jonathan Illanes
- Matías Rojas
- Lucas Saá