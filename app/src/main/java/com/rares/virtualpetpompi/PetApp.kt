package com.rares.virtualpetpompi

import android.app.Application
import android.content.Context
import com.rares.virtualpetpompi.data.GameRepository
import com.rares.virtualpetpompi.data.GameStore
import com.rares.virtualpetpompi.data.PetDatabase
import com.rares.virtualpetpompi.data.StepCounter
import com.rares.virtualpetpompi.data.gameDataStore
import com.rares.virtualpetpompi.work.Notifications
import com.rares.virtualpetpompi.work.PetCheckWorker

/** Hand-wired dependencies; small enough that a DI framework would only add noise. */
class AppContainer(context: Context) {
    val stepCounter = StepCounter(context)
    val repository = GameRepository(GameStore(gameDataStore(context)), PetDatabase.create(context))
}

class PetApp : Application() {
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        Notifications.ensureChannel(this)
        PetCheckWorker.schedule(this)
    }
}
