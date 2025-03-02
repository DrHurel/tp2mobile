package fr.hureljeremy.gitea.tp2mobile.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

class AccelerometerService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f
    private var movementThreshold = 1.5f
    private var accelerationCallback: ((Float) -> Unit)? = null


    inner class LocalBinder : Binder() {
        fun getService(): AccelerometerService = this@AccelerometerService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Log.e("AccelerometerService", "Accelerometer sensor not available on this device")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun isAccelerometerAvailable(): Boolean {
        return accelerometer != null
    }

    fun getAccelerometerData(): Triple<Float, Float, Float> {
        return Triple(lastX, lastY, lastZ)
    }

    fun isMovementDetected(): Boolean {
        return abs(lastX) > movementThreshold || abs(lastY) > movementThreshold || abs(lastZ) > movementThreshold
    }

    fun isMovingUp(): Boolean = lastY < -movementThreshold
    fun isMovingDown(): Boolean = lastY > movementThreshold
    fun isMovingLeft(): Boolean = lastX < -movementThreshold
    fun isMovingRight(): Boolean = lastX > movementThreshold


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
       return
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }


    fun setAccelerationCallback(callback: (Float) -> Unit) {
        accelerationCallback = callback
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            lastX = event.values[0]
            lastY = event.values[1]
            lastZ = event.values[2]
            val acceleration = sqrt(lastX * lastX + lastY * lastY + lastZ * lastZ)
            accelerationCallback?.invoke(acceleration)
        }
    }


}