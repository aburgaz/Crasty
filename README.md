# Crasty

App personal de tareas (ToDo) para Android. Sencilla, 100% offline y con la opción de **procrastinar**: pasar una tarea al día siguiente.

## Funcionalidades

- Pestañas **Hoy** y **Mañana**. Crear, editar, completar y eliminar tareas.
- Procrastinar deslizando: en Hoy, una tarea deslizada a la derecha pasa a mañana; en Mañana, deslizada a la izquierda vuelve a hoy.
- **Tareas recurrentes**: se repiten cada N días, semanas o meses. Se completan día a día, no se pueden procrastinar y no aparecen en Completadas.
- **Proyectos**: listas de tareas con fecha y hora límite. Sus tareas deben caer antes de esa fecha. Pestañas En curso / Completados.
- **Completadas**: tareas hechas agrupadas por día, filtrando por mes y año. Al desmarcar una vuelve a Hoy.
- **Calendario**: eliges un día y ves, añades o editas sus tareas.
- Recordatorios con notificación local a una hora concreta (también en las recurrentes).
- Tema claro, oscuro o según el sistema.
- Exportar/importar tareas y proyectos en un archivo JSON para pasarlos a otro móvil.

## Stack

| Pieza | Elección | Por qué |
|---|---|---|
| Lenguaje | Kotlin | Lenguaje oficial de Android |
| UI | Jetpack Compose + Material 3 | UI declarativa moderna; colores dinámicos del Pixel |
| Base de datos | Room (SQLite) | Local, sin internet, con consultas reactivas (`Flow`) y migraciones automáticas |
| Navegación | Navigation Compose (rutas tipadas) | Estándar de Google |
| Recordatorios | `AlarmManager` + notificaciones | Alarmas exactas sin internet ni servicios externos |
| Copia de seguridad | kotlinx.serialization (JSON) + Storage Access Framework | El archivo lo guardas donde quieras (Descargas, Drive, USB...) |
| Preferencias | SharedPreferences | Solo guarda el tema |
| Inyección de dependencias | Manual (`AppContainer`) | Sin Hilt: no hace falta para una app de este tamaño |

`minSdk 31` (Android 12) · `targetSdk 37` · AGP 9 · Gradle 9.

## Arquitectura

Capas simples (MVVM): **UI (Compose) → ViewModel → Repositorio → Room / AlarmManager**.

```
app/src/main/java/com/crasty/app/
├── CrastyApp.kt              # Application: crea el AppContainer y el canal de notificaciones
├── MainActivity.kt           # Única Activity; aplica el tema y pide permiso de notificaciones
├── di/AppContainer.kt        # Dependencias (BD, repositorios, recordatorios, backup)
├── domain/model/             # Task, Project, Recurrence (reglas de repetición y fecha límite)
├── data/
│   ├── local/                # Room: entidades, DAOs y CrastyDatabase
│   ├── repository/           # TaskRepository: única puerta a tareas y proyectos
│   ├── settings/             # Preferencias (tema)
│   └── backup/               # Exportar/importar JSON
├── reminders/                # AlarmManager, notificación y reprogramación al reiniciar
└── ui/
    ├── theme/                # Tema Material 3
    ├── navigation/           # Rutas entre pantallas
    ├── components/           # Lista de tareas (con deslizar), diálogos de fecha/hora, utilidades
    └── screens/
        ├── home/             # Hoy / Mañana
        ├── calendar/         # Calendario + tareas del día elegido
        ├── completed/        # Tareas completadas por mes
        ├── projects/         # Lista, detalle y editor de proyectos
        ├── editor/           # Crear / editar / eliminar tarea
        └── settings/         # Tema y exportar / importar
```

`app/schemas/` guarda el esquema de cada versión de la BD que genera Room. Lo usan las migraciones automáticas para conservar los datos al actualizar la app.

## Cómo compilar e instalar

1. Instala [Android Studio](https://developer.android.com/studio) (trae el JDK y el Android SDK).
2. Abre esta carpeta con Android Studio y espera a que sincronice Gradle.
3. En el Pixel: *Ajustes → Información del teléfono →* pulsa 7 veces en *Número de compilación*. Después, en *Sistema → Opciones de desarrollador*, activa *Depuración USB* (o *Depuración inalámbrica*).
4. Conecta el móvil y pulsa ▶ *Run*.

Desde la terminal: `./gradlew installDebug` (o `assembleRelease` para generar el APK en `app/build/outputs/apk/release/`).

## Pasar los datos a otro móvil

*Ajustes → Exportar tareas* en el móvil viejo → copia el `.json` al nuevo → *Ajustes → Importar tareas*. Al importar se sustituye todo lo actual. Los archivos exportados con versiones anteriores de la app se siguen pudiendo importar.
