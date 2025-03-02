package fr.hureljeremy.gitea.tp2mobile.ex5

    import android.content.Intent
    import android.os.Bundle
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import fr.hureljeremy.gitea.tp2mobile.MainActivity
    import fr.hureljeremy.gitea.tp2mobile.R
    import fr.hureljeremy.gitea.tp2mobile.services.AccelerometerService

    class Shaking : AppCompatActivity() {

        private lateinit var accelerometer: AccelerometerService
        private var bound = false
        private var flashEnabled = false
        private lateinit var cameraManager: android.hardware.camera2.CameraManager
        private var cameraId: String? = null
        private lateinit var flashlightImageView: android.widget.ImageView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_shaking)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }


            cameraManager = getSystemService(android.hardware.camera2.CameraManager::class.java)
            cameraId = cameraManager.cameraIdList.firstOrNull()

            flashlightImageView = findViewById(R.id.flashlightImageView)
            findViewById<android.widget.Button>(R.id.btn_home).setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        private val connection = object : android.content.ServiceConnection {
            override fun onServiceConnected(name: android.content.ComponentName, service: android.os.IBinder) {
                val binder = service as AccelerometerService.LocalBinder
                accelerometer = binder.getService()
                bound = true
                setupShakeDetection()
            }

            override fun onServiceDisconnected(name: android.content.ComponentName) {
                bound = false
            }
        }

        private fun setupShakeDetection() {
            accelerometer.setAccelerationCallback { acceleration ->
                if (acceleration > 20) {
                    toggleFlash()
                }
            }
        }

        private fun toggleFlash() {
            cameraId?.let {
                try {
                    flashEnabled = !flashEnabled
                    cameraManager.setTorchMode(it, flashEnabled)
                    runOnUiThread {
                        flashlightImageView.isActivated = flashEnabled
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onStart() {
            super.onStart()
            android.content.Intent(this, AccelerometerService::class.java).also { intent ->
                bindService(intent, connection, android.content.Context.BIND_AUTO_CREATE)
            }
        }

        override fun onStop() {
            super.onStop()
            if (bound) {
                unbindService(connection)
                bound = false
            }
            if (flashEnabled) {
                cameraId?.let {
                    try {
                        cameraManager.setTorchMode(it, false)
                        flashEnabled = false
                        flashlightImageView.isActivated = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }