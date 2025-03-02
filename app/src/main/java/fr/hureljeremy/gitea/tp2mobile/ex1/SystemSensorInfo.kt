package fr.hureljeremy.gitea.tp2mobile.ex1

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.NavigationService
import fr.hureljeremy.gitea.tp2mobile.services.SensorService

class SystemSensorInfo : AppCompatActivity() {

    private lateinit var sensorService: SensorService
    private lateinit var navigationService: NavigationService
    private var bound = false
    private lateinit var sensorContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_sensor_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorContainer = findViewById(R.id.sensor_container)

        val btnHome = findViewById<Button>(R.id.btn_home)
        btnHome.setOnClickListener {
            if (::navigationService.isInitialized) {
                navigationService.navigate(this, "home")
            }
        }
    }

    private fun displaySensorInfo(sensor: Sensor) {
        val sensorInfoView = TextView(this).apply {
            text = buildString {
                append("Name: ${sensor.name}\n")
                append("Type: ${sensor.stringType}\n")
                append("Vendor: ${sensor.vendor}\n")
                append("Version: ${sensor.version}\n")
                append("Power: ${sensor.power} mA\n")
                append("Maximum Range: ${sensor.maximumRange}\n")
                append("Resolution: ${sensor.resolution}")
            }
            setTextColor(resources.getColor(android.R.color.black, theme))
            textSize = 16f
            setPadding(32, 16, 32, 16)
            background = resources.getDrawable(R.drawable.rounded_background, theme)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
        }
        sensorContainer.addView(sensorInfoView)
    }

    private val sensorConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as SensorService.LocalBinder
            sensorService = binder.getService()
            bound = true

            sensorService.getAvailableSensors().forEach { sensor ->
                displaySensorInfo(sensor)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }
    }

    private val navigationConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun onStart() {
        super.onStart()
        Intent(this, SensorService::class.java).also { intent ->
            bindService(intent, sensorConnection, Context.BIND_AUTO_CREATE)
        }
        Intent(this, NavigationService::class.java).also { intent ->
            bindService(intent, navigationConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(sensorConnection)
            unbindService(navigationConnection)
            bound = false
        }
    }
}