package com.crasty.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.screens.calendar.CalendarScreen
import com.crasty.app.ui.screens.completed.CompletedScreen
import com.crasty.app.ui.screens.editor.TaskEditorScreen
import com.crasty.app.ui.screens.home.HomeScreen
import com.crasty.app.ui.screens.projects.ProjectDetailScreen
import com.crasty.app.ui.screens.projects.ProjectEditorScreen
import com.crasty.app.ui.screens.projects.ProjectsScreen
import com.crasty.app.ui.screens.settings.SettingsScreen
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable object HomeRoute
@Serializable object CalendarRoute
@Serializable object CompletedRoute
@Serializable object ProjectsRoute
@Serializable object SettingsRoute

@Serializable data class ProjectDetailRoute(val projectId: Long)

/** projectId = 0 crea un proyecto nuevo. */
@Serializable data class ProjectEditorRoute(val projectId: Long = 0L)

/** taskId = 0 crea una tarea nueva en [dateEpochDay]. projectId = 0: sin proyecto. */
@Serializable data class EditorRoute(val taskId: Long = 0L, val dateEpochDay: Long, val projectId: Long = 0L)

@Composable
fun CrastyNavHost() {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    val openTask = { task: Task ->
        navController.navigate(EditorRoute(task.id, task.date.toEpochDay(), task.projectId ?: 0L))
    }
    val newTask = { date: LocalDate -> navController.navigate(EditorRoute(dateEpochDay = date.toEpochDay())) }
    val newProject = { navController.navigate(ProjectEditorRoute()) }

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenTask = openTask,
                onNewTask = newTask,
                onNewProject = newProject,
                onOpenProjects = { navController.navigate(ProjectsRoute) },
                onOpenCompleted = { navController.navigate(CompletedRoute) },
                onOpenCalendar = { navController.navigate(CalendarRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<CalendarRoute> {
            CalendarScreen(onBack = back, onOpenTask = openTask, onNewTask = newTask)
        }
        composable<CompletedRoute> {
            CompletedScreen(onBack = back, onOpenTask = openTask)
        }
        composable<ProjectsRoute> {
            ProjectsScreen(
                onBack = back,
                onOpenProject = { navController.navigate(ProjectDetailRoute(it)) },
                onNewProject = newProject,
            )
        }
        composable<ProjectDetailRoute> { entry ->
            val projectId = entry.toRoute<ProjectDetailRoute>().projectId
            ProjectDetailScreen(
                projectId = projectId,
                onBack = back,
                onEditProject = { navController.navigate(ProjectEditorRoute(projectId)) },
                onOpenTask = openTask,
                onNewTask = { navController.navigate(EditorRoute(dateEpochDay = LocalDate.now().toEpochDay(), projectId = projectId)) },
            )
        }
        composable<ProjectEditorRoute> { entry ->
            ProjectEditorScreen(
                projectId = entry.toRoute<ProjectEditorRoute>().projectId,
                onBack = back,
                onSaved = { id, isNew ->
                    if (isNew) {
                        // Un proyecto recién creado se abre directamente para añadirle tareas.
                        navController.navigate(ProjectDetailRoute(id)) {
                            popUpTo<ProjectEditorRoute> { inclusive = true }
                        }
                    } else {
                        back()
                    }
                },
            )
        }
        composable<EditorRoute> { entry ->
            val route = entry.toRoute<EditorRoute>()
            TaskEditorScreen(
                taskId = route.taskId,
                initialDate = LocalDate.ofEpochDay(route.dateEpochDay),
                projectId = route.projectId,
                onDone = back,
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(onBack = back)
        }
    }
}
