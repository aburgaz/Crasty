package com.crasty.app.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.crasty.app.R

object Notifications {
    const val REMINDER_CHANNEL_ID = "reminders"

    fun createChannels(context: Context) {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.reminder_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
