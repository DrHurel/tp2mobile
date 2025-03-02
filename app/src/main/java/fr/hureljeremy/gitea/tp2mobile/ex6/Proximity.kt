package fr.hureljeremy.gitea.tp2mobile.ex6

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.ProximitySensorService

class Proximity : AppCompatActivity() {

    private lateinit var proximityService: ProximitySensorService
    private var bound = false
    private lateinit var proximityImage: ImageView
    private lateinit var proximityStatusText: android.widget.TextView
    private lateinit var homeButton: android.widget.Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_proximity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        proximityImage = findViewById(R.id.proximityImage)
        proximityStatusText = findViewById(R.id.proximityStatusText)
        homeButton = findViewById(R.id.btn_home)

        homeButton.setOnClickListener {
            finish()
        }
    }

    private val connection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName, service: android.os.IBinder) {
            val binder = service as ProximitySensorService.LocalBinder
            proximityService = binder.getService()
            bound = true
            setupProximitySensor()
        }

        override fun onServiceDisconnected(name: android.content.ComponentName) {
            bound = false
        }
    }

    private fun setupProximitySensor() {
        proximityService.setProximityCallback { isNear ->
            runOnUiThread {
                proximityImage.setImageResource(
                    if (isNear) R.drawable.near else R.drawable.far
                )
                proximityStatusText.text = if (isNear) "Near" else "Far"
                proximityStatusText.setTextColor(
                    if (isNear) getColor(android.R.color.holo_red_dark)
                    else getColor(android.R.color.holo_blue_dark)
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        android.content.Intent(this, ProximitySensorService::class.java).also { intent ->
            bindService(intent, connection, android.content.Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }
}