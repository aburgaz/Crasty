package com.crasty.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.crasty.app.R
import com.crasty.app.ui.screens.calendar.CalendarScreen
import com.crasty.app.ui.screens.day.DayScreen
import com.crasty.app.ui.screens.editor.TaskEditorScreen
import com.crasty.app.ui.screens.settings.SettingsScreen
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable object TodayRoute
@Serializable object TomorrowRoute
@Serializable object CalendarRoute
@Serializable object SettingsRoute

/** taskId = 0 crea una tarea nueva en [dateEpochDay]. */
@Serializable data class EditorRoute(val taskId: Long = 0L, val dateEpochDay: Long)

private data class Tab(val route: Any, val label: Int, val icon: ImageVector)

private val tabs = listOf(
    Tab(TodayRoute, R.string.tab_today, Icons.Outlined.Today),
    Tab(TomorrowRoute, R.string.tab_tomorrow, Icons.Outlined.WbSunny),
    Tab(CalendarRoute, R.string.tab_calendar, Icons.Outlined.CalendarMonth),
)

@Composable
fun CrastyNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomBar = tabs.any { destination?.hasRoute(it.route::class) == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        // Cada pantalla gestiona su barra superior; aquí solo se reserva el hueco de la barra inferior.
        val bottom = PaddingValues(bottom = padding.calculateBottomPadding())
        val openEditor = { taskId: Long, date: LocalDate ->
            navController.navigate(EditorRoute(taskId, date.toEpochDay()))
        }
        NavHost(
            navController = navController,
            startDestination = TodayRoute,
            modifier = Modifier.padding(bottom).consumeWindowInsets(bottom),
        ) {
            composable<TodayRoute> {
                DayScreen(
                    date = LocalDate.now(),
                    title = stringResource(R.string.tab_today),
                    onEditTask = openEditor,
                    onOpenSettings = { navController.navigate(SettingsRoute) },
                )
            }
            composable<TomorrowRoute> {
                DayScreen(
                    date = LocalDate.now().plusDays(1),
                    title = stringResource(R.string.tab_tomorrow),
                    onEditTask = openEditor,
                    onOpenSettings = { navController.navigate(SettingsRoute) },
                )
            }
            composable<CalendarRoute> {
                CalendarScreen(onEditTask = openEditor)
            }
            composable<EditorRoute> { entry ->
                val route = entry.toRoute<EditorRoute>()
                TaskEditorScreen(
                    taskId = route.taskId,
                    initialDate = LocalDate.ofEpochDay(route.dateEpochDay),
                    onDone = { navController.popBackStack() },
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
