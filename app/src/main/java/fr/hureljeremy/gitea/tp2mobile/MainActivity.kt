package fr.hureljeremy.gitea.tp2mobile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.hureljeremy.gitea.tp2mobile.ex1.SystemSensorInfo
import fr.hureljeremy.gitea.tp2mobile.ex2.MissingSensorInfo
import fr.hureljeremy.gitea.tp2mobile.ex3.Accelerometer
import fr.hureljeremy.gitea.tp2mobile.ex4.Direction
import fr.hureljeremy.gitea.tp2mobile.ex5.Shaking
import fr.hureljeremy.gitea.tp2mobile.ex6.Proximity
import fr.hureljeremy.gitea.tp2mobile.ex7.Maps
import fr.hureljeremy.gitea.tp2mobile.ex8.CountriesList
import fr.hureljeremy.gitea.tp2mobile.ex8.CountryInfo
import fr.hureljeremy.gitea.tp2mobile.ex9.CountryActivity
import fr.hureljeremy.gitea.tp2mobile.services.NavigationService

data class Destination(val name: String, val activity: Class<out AppCompatActivity>)

class MainActivity : AppCompatActivity() {

    lateinit var navigationService: NavigationService
    private var bound = false

    private val pages = listOf(
        Destination("home", MainActivity::class.java),
        Destination("system-sensor-info", SystemSensorInfo::class.java),
        Destination("missing-sensor-info", MissingSensorInfo::class.java),
        Destination("accelerometer", Accelerometer::class.java),
        Destination("direction", Direction::class.java),
        Destination("shaking", Shaking::class.java),
        Destination("proximity", Proximity::class.java),
        Destination("maps", Maps::class.java),
        Destination("countries-list", CountriesList::class.java),
        Destination("country-info", CountryInfo::class.java),
        Destination("countries-list-fragment", CountryActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val container = findViewById<LinearLayout>(R.id.button_container)
        for (page in pages) {
            if (page.name == "home" || page.name == "country-info") {
                continue
            }
            val button = Button(this).apply {
                appNavBtn(page)
            }
            container.addView(button)
        }
    }

    private fun Button.appNavBtn(page: Destination) {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(32, 16, 32, 16)
        }

        setBackgroundResource(R.drawable.custom_button_background)
        setTextColor(resources.getColor(android.R.color.white, theme))
        textSize = 16f

        setPadding(32, 24, 32, 24)

        text = page.name.split('-').joinToString(" ") { it ->
            it.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }

        setOnClickListener {
            if (bound) {
                navigationService.navigate(context, page.name)
            }
        }
    }
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()
            if (!bound) {
                val registered = navigationService.getDestinations()
                for (page in pages) {
                    if (registered.contains(page.name)) {
                        continue
                    }
                    navigationService.registerDestination(page.name, page.activity)
                }
            }
            bound = true


        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, NavigationService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)

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
