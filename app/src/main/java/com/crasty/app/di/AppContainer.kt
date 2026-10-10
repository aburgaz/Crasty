package com.crasty.app.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.crasty.app.CrastyApp
import com.crasty.app.data.backup.BackupManager
import com.crasty.app.data.local.CrastyDatabase
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.reminders.ReminderScheduler

/** Inyección de dependencias manual: suficiente para una app de este tamaño (sin Hilt). */
class AppContainer(context: Context) {
    private val database = CrastyDatabase.create(context)
    private val reminderScheduler = ReminderScheduler(context)
    val taskRepository = TaskRepository(database.taskDao(), reminderScheduler)
    val backupManager = BackupManager(context, taskRepository)
}

/** Acceso al contenedor desde las factorías de ViewModel. */
fun CreationExtras.appContainer(): AppContainer = (this[APPLICATION_KEY] as CrastyApp).container
