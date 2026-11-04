package com.rares.virtualpetpompi.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads the hardware step counter. The sensor keeps counting while the app is not running, so
 * there is no need for a foreground service: we just sample it when the app opens and from a
 * periodic worker. Values are cumulative since the last device boot.
 */
class StepCounter(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val isAvailable: Boolean get() = sensor != null

    /** Emits the counter every time it changes while collected. */
    fun updates(): Flow<Long> = callbackFlow {
        val s = sensor
        if (s == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event.values[0].toLong())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, s, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }.conflate()

    /** One reading, or null if the sensor is missing, not permitted, or silent for [timeoutMs]. */
    suspend fun readOnce(timeoutMs: Long = 10_000): Long? =
        withTimeoutOrNull(timeoutMs) { updates().first() }
}
