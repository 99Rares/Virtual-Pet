package com.rares.virtualpetpompi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * adb shell am broadcast -n com.rares.virtualpetpompi/.DebugStepReceiver --el counter 12345
 * Pretends the hardware step counter now reads [counter]. Only exists in debug builds.
 */
class DebugStepReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val counter = intent.getLongExtra("counter", -1L)
        if (counter < 0) return
        val repo = (context.applicationContext as PetApp).container.repository
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                repo.recordCounter(counter)
            } finally {
                pending.finish()
            }
        }
    }
}
