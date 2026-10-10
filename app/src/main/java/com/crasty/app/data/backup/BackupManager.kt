package com.crasty.app.data.backup

import android.content.Context
import android.net.Uri
import com.crasty.app.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Exporta/importa tareas y proyectos a un archivo JSON elegido por el usuario
 * (Storage Access Framework). Sirve para pasarlos a otro móvil sin internet.
 */
class BackupManager(
    private val context: Context,
    private val repository: TaskRepository,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Devuelve el número de tareas exportadas. */
    suspend fun export(uri: Uri): Int = withContext(Dispatchers.IO) {
        val file = repository.snapshot().toBackup(Instant.now())
        context.contentResolver.openOutputStream(uri, "wt")!!.use {
            it.write(json.encodeToString(BackupFile.serializer(), file).toByteArray())
        }
        file.tasks.size
    }

    /** Sustituye todo lo actual por el contenido del archivo. Devuelve cuántas tareas se importaron. */
    suspend fun import(uri: Uri): Int = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
        val file = json.decodeFromString(BackupFile.serializer(), text)
        repository.replaceAll(file.toSnapshot())
        file.tasks.size
    }
}
