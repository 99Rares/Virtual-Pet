package com.rares.virtualpetpompi.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rares.virtualpetpompi.PetApp
import com.rares.virtualpetpompi.domain.PetRules
import java.util.concurrent.TimeUnit

/**
 * Runs about every 30 minutes in the background: samples the step counter, ages the pet, and
 * sends one reminder when it gets hungry. The reminder re-arms after the pet is fed.
 */
class PetCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as PetApp).container
        val repo = container.repository

        container.stepCounter.readOnce()?.let { repo.recordCounter(it) }
        repo.settle()

        val snap = repo.currentSnapshot()
        if (snap.isOnboarded && !snap.pet.isDead) {
            val hungry = snap.pet.hunger <= PetRules.REMINDER_THRESHOLD
            if (hungry && !snap.reminderSent) {
                if (Notifications.postHungry(applicationContext)) repo.setReminderSent(true)
            } else if (!hungry && snap.reminderSent) {
                repo.setReminderSent(false)
            }
        }
        return Result.success()
    }

    companion object {
        private const val NAME = "pet-check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PetCheckWorker>(30, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
