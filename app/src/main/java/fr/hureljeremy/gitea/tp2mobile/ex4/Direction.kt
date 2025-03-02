package fr.hureljeremy.gitea.tp2mobile.ex4

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.AccelerometerService
import fr.hureljeremy.gitea.tp2mobile.services.NavigationService

class Direction : AppCompatActivity() {

    private lateinit var navigationService: NavigationService
    private lateinit var accelerometerService: AccelerometerService
    private lateinit var arrowImageView: ImageView
    private lateinit var directionTextView: TextView
    private lateinit var valuesTextView: TextView
    private var bound = false

    private val navigationConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    private val accelerometerConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AccelerometerService.LocalBinder
            accelerometerService = binder.getService()
            bound = true
            startDirectionDetection()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_direction)

        arrowImageView = findViewById(R.id.arrowImageView)
        directionTextView = findViewById(R.id.directionTextView)
        valuesTextView = findViewById(R.id.valuesTextView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btn_home).setOnClickListener {
            navigationService.navigate(this, "home")
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

    private fun startDirectionDetection() {
        var isRunning = true
        Thread {
            while (isRunning) {
                try {
                    runOnUiThread {
                        val (x, y, z) = accelerometerService.getAccelerometerData()
                        valuesTextView.text = String.format("X: %.2f\nY: %.2f\nZ: %.2f", x, y, z)

                        when {
                            accelerometerService.isMovingUp() -> {
                                arrowImageView.rotation = 0f
                                directionTextView.text = getString(R.string.moving_up)
                            }
                            accelerometerService.isMovingDown() -> {
                                arrowImageView.rotation = 180f
                                directionTextView.text = getString(R.string.moving_down)
                            }
                            accelerometerService.isMovingLeft() -> {
                                arrowImageView.rotation = 270f
                                directionTextView.text = getString(R.string.moving_left)
                            }
                            accelerometerService.isMovingRight() -> {
                                arrowImageView.rotation = 90f
                                directionTextView.text = getString(R.string.moving_right)
                            }
                            else -> {
                                directionTextView.text = getString(R.string.stable)
                            }
                        }
                    }
                    Thread.sleep(50) // Update more frequently
                } catch (e: Exception) {
                    isRunning = false
                }
            }
        }.start()
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