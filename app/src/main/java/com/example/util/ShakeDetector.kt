package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Emergency Back-to-Calc Trigger:
 * Detects rapid shaking of the device via the hardware accelerometer.
 * When a high-velocity shake gesture occurs, immediately triggers an emergency callback
 * to wipe active memory buffers and snap the UI back to the innocent 3D Calculator.
 */
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {
    private var lastShakeTimestamp: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isFirst = true

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (isFirst) {
            lastX = x
            lastY = y
            lastZ = z
            isFirst = false
            return
        }

        val deltaX = x - lastX
        val deltaY = y - lastY
        val deltaZ = z - lastZ

        lastX = x
        lastY = y
        lastZ = z

        val deltaVector = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble())
        val now = System.currentTimeMillis()

        // Threshold of rapid directional acceleration
        if (deltaVector > 13.5 && now - lastShakeTimestamp > 900) {
            lastShakeTimestamp = now
            onShake()
        }
    }

    companion object {
        fun register(context: Context, detector: ShakeDetector): SensorManager? {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer != null) {
                sensorManager.registerListener(detector, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }
            return sensorManager
        }

        fun unregister(sensorManager: SensorManager?, detector: ShakeDetector) {
            sensorManager?.unregisterListener(detector)
        }
    }
}
