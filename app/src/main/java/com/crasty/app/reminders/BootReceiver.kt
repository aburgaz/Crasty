package com.crasty.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crasty.app.CrastyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Las alarmas se pierden al reiniciar el móvil o actualizar la app: aquí se vuelven a programar. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val repository = (context.applicationContext as CrastyApp).container.taskRepository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.rescheduleAllReminders()
            } finally {
                pending.finish()
            }
        }
    }
}
