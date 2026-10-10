package com.crasty.app.data.backup

import android.content.Context
import android.net.Uri
import com.crasty.app.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Exporta/importa todas las tareas a un archivo JSON elegido por el usuario
 * (Storage Access Framework). Sirve para pasar las tareas a otro móvil sin internet.
 */
class BackupManager(
    private val context: Context,
    private val repository: TaskRepository,
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /** Devuelve el número de tareas exportadas. */
    suspend fun export(uri: Uri): Int = withContext(Dispatchers.IO) {
        val tasks = repository.getAll()
        val file = BackupFile(exportedAt = Instant.now().toString(), tasks = tasks.map { it.toBackup() })
        context.contentResolver.openOutputStream(uri, "wt")!!.use {
            it.write(json.encodeToString(BackupFile.serializer(), file).toByteArray())
        }
        tasks.size
    }

    /** Sustituye todas las tareas actuales por las del archivo. Devuelve cuántas se importaron. */
    suspend fun import(uri: Uri): Int = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
        val file = json.decodeFromString(BackupFile.serializer(), text)
        repository.replaceAll(file.tasks.map { it.toTask() })
        file.tasks.size
    }
}
