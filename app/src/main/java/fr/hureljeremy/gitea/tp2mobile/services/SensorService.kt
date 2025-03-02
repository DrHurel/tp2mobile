package fr.hureljeremy.gitea.tp2mobile.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log

class SensorService : Service() {

    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private val allSensors = listOf(
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_GYROSCOPE,
        Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_LIGHT,
        Sensor.TYPE_PRESSURE,
        Sensor.TYPE_PROXIMITY,
        Sensor.TYPE_GRAVITY,
        Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_ROTATION_VECTOR
    )

    inner class LocalBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun getAvailableSensors(): List<Sensor> {
        val availableSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val sensorNames = availableSensors.map { it.name }
        Log.d("SensorService", "Available Sensors: $sensorNames")
        return availableSensors
    }

    fun getUnavailableSensors(): List<String> {
        val availableSensors = sensorManager.getSensorList(Sensor.TYPE_ALL).map { it.type }
        val unavailableSensors = allSensors.filterNot { availableSensors.contains(it) }
            .map { sensorTypeToString(it) }
        Log.d("SensorService", "Unavailable Sensors: $unavailableSensors")
        return unavailableSensors
    }

    private fun sensorTypeToString(sensorType: Int): String {
        return when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetic Field"
            Sensor.TYPE_LIGHT -> "Light Sensor"
            Sensor.TYPE_PRESSURE -> "Barometer"
            Sensor.TYPE_PROXIMITY -> "Proximity Sensor"
            Sensor.TYPE_GRAVITY -> "Gravity Sensor"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            else -> "Unknown Sensor"
        }
    }
}
