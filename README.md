# Crasty 🦥

App personal de tareas (ToDo) para Android, inspirada en Slothy. Sencilla, 100% offline y con la opción de **procrastinar**: pasar una tarea al día siguiente.

## Funcionalidades

- Crear, editar, completar y eliminar tareas.
- Procrastinar una tarea (pasa al día siguiente, y su recordatorio con ella).
- Vistas **Hoy**, **Mañana** y **Calendario** (eliges un día y ves, añades o editas sus tareas).
- Recordatorios con notificación local a una hora concreta.
- Exportar/importar todas las tareas en un archivo JSON para pasarlas a otro móvil.

## Stack

| Pieza | Elección | Por qué |
|---|---|---|
| Lenguaje | Kotlin | Lenguaje oficial de Android |
| UI | Jetpack Compose + Material 3 | UI declarativa moderna; colores dinámicos del Pixel |
| Base de datos | Room (SQLite) | Local, sin internet, con consultas reactivas (`Flow`) |
| Navegación | Navigation Compose (rutas tipadas) | Estándar de Google |
| Recordatorios | `AlarmManager` + notificaciones | Alarmas exactas sin internet ni servicios externos |
| Copia de seguridad | kotlinx.serialization (JSON) + Storage Access Framework | El archivo lo guardas donde quieras (Descargas, Drive, USB...) |
| Inyección de dependencias | Manual (`AppContainer`) | Sin Hilt: no hace falta para una app de este tamaño |

`minSdk 31` (Android 12) · `targetSdk 37` · AGP 9 · Gradle 9.

## Arquitectura

Capas simples (MVVM): **UI (Compose) → ViewModel → Repositorio → Room / AlarmManager**.

```
app/src/main/java/com/crasty/app/
├── CrastyApp.kt              # Application: crea el AppContainer y el canal de notificaciones
├── MainActivity.kt           # Única Activity; pide permiso de notificaciones
├── di/AppContainer.kt        # Dependencias (BD, repositorio, recordatorios, backup)
├── domain/model/Task.kt      # Modelo de tarea
├── data/
│   ├── local/                # Room: TaskEntity, TaskDao, CrastyDatabase
│   ├── repository/           # TaskRepository: única puerta a las tareas
│   └── backup/               # Exportar/importar JSON
├── reminders/                # AlarmManager, notificación y reprogramación al reiniciar
└── ui/
    ├── theme/                # Tema Material 3
    ├── navigation/           # Rutas y barra inferior (Hoy / Mañana / Calendario)
    ├── components/           # Lista de tareas reutilizable, utilidades de fechas
    └── screens/
        ├── day/              # Hoy y Mañana (misma pantalla, distinta fecha)
        ├── calendar/         # Calendario + tareas del día elegido
        ├── editor/           # Crear / editar / eliminar tarea
        └── settings/         # Exportar / importar
```

`app/schemas/` guarda el esquema de la BD que genera Room; sirve para las migraciones cuando cambie el modelo.

## Cómo compilar e instalar

1. Instala [Android Studio](https://developer.android.com/studio) (trae el JDK y el Android SDK).
2. Abre esta carpeta con Android Studio y espera a que sincronice Gradle.
3. En el Pixel: *Ajustes → Información del teléfono →* pulsa 7 veces en *Número de compilación*. Después, en *Opciones de desarrollador*, activa *Depuración USB* (o *Depuración inalámbrica*).
4. Conecta el móvil y pulsa ▶ *Run*.

Desde la terminal: `./gradlew installDebug` (o `assembleRelease` para generar el APK en `app/build/outputs/apk/release/`).

## Pasar las tareas a otro móvil

*Ajustes → Exportar tareas* en el móvil viejo → copia el `.json` al nuevo → *Ajustes → Importar tareas*. Al importar se sustituyen todas las tareas actuales.
