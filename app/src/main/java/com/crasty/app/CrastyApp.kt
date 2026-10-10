package com.crasty.app

import android.app.Application
import com.crasty.app.di.AppContainer
import com.crasty.app.reminders.Notifications

class CrastyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannels(this)
    }
}
