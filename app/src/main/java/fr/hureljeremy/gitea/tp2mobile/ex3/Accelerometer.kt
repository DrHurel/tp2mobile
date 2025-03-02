package fr.hureljeremy.gitea.tp2mobile.ex3

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.AccelerometerService
import fr.hureljeremy.gitea.tp2mobile.services.NavigationService

class Accelerometer : AppCompatActivity() {

    private lateinit var navigationService: NavigationService
    private lateinit var accelerometerService: AccelerometerService
    private lateinit var magnitudeTextView: TextView
    private var bound = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_accelerometer)
        val rootView = findViewById<View>(R.id.main)
        magnitudeTextView = findViewById<TextView>(R.id.magnitude_text)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnHome = findViewById<Button>(R.id.btn_home)
        btnHome.setOnClickListener {
            navigationService.navigate(this, "home")
        }
    }

    private val navigationConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    private val accelerometerConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as AccelerometerService.LocalBinder
            accelerometerService = binder.getService()
            bound = true
            accelerometerService.setAccelerationCallback { acceleration ->
                val color = when {
                    acceleration < 5f -> Color.GREEN
                    acceleration < 15f -> Color.BLACK
                    else -> Color.RED
                }
                runOnUiThread {
                    findViewById<View>(R.id.main).setBackgroundColor(color)
                    magnitudeTextView.text = String.format("Acceleration: %.2f m/s²", acceleration)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, NavigationService::class.java).also { intent ->
            bindService(intent, navigationConnection, Context.BIND_AUTO_CREATE)
        }
        Intent(this, AccelerometerService::class.java).also { intent ->
            bindService(intent, accelerometerConnection, Context.BIND_AUTO_CREATE)
        }


    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(navigationConnection)
            unbindService(accelerometerConnection)
            bound = false
        }
    }


}