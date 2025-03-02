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

class ProximitySensorService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    private var lastProximityValue: Float = -1f

    inner class LocalBinder : Binder() {
        fun getService(): ProximitySensorService = this@ProximitySensorService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Log.e("ProximityService", "Proximity sensor not available on this device")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun isProximitySensorAvailable(): Boolean {
        return proximitySensor != null
    }

    fun getProximitySensorData(): Float {
        return lastProximityValue
    }

    fun isObjectDetected(): Boolean {
        return lastProximityValue in 0f..(proximitySensor?.maximumRange ?: 5f)
    }

    fun isObjectNear(): Boolean {
        return lastProximityValue in 0f..(proximitySensor?.maximumRange ?: 5f) * 0.75f
    }

    fun isObjectFar(): Boolean {
        return lastProximityValue >= (proximitySensor?.maximumRange ?: 5f) * 0.75f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            lastProximityValue = event.values[0]
            val isNear = isObjectNear()
            proximityCallback?.invoke(isNear)
            Log.d("ProximityService", "Proximity Sensor Value: $lastProximityValue")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return // not used
    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    private var proximityCallback: ((Boolean) -> Unit)? = null

    fun setProximityCallback(callback: (Boolean) -> Unit) {
        proximityCallback = callback
    }


}
