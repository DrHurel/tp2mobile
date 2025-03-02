package fr.hureljeremy.gitea.tp2mobile.ex8

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.CountryService
import android.widget.Button


class CountryInfo : AppCompatActivity() {
    private lateinit var countryService: CountryService
    private var boundToCountryService = false
    private lateinit var detailsTextView: TextView
    private lateinit var btnHome: Button

    private val countryServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CountryService.LocalBinder
            countryService = binder.getService()
            boundToCountryService = true
            loadCountryDetails()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundToCountryService = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_country_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupHomeButton()
        bindCountryService()
    }

    private fun setupViews() {
        detailsTextView = findViewById(R.id.countryDetailsTextView)
        btnHome = findViewById(R.id.btn_home)
    }

    private fun setupHomeButton() {
        btnHome.setOnClickListener {
            finish()
        }
    }

    private fun bindCountryService() {
        Intent(this, CountryService::class.java).also { intent ->
            bindService(intent, countryServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun loadCountryDetails() {
        if (boundToCountryService) {
            val countryCode = intent.getStringExtra("countryCode")
            countryCode?.let { code ->
                countryService.getCountryDetails(code) { details ->
                    runOnUiThread {
                        detailsTextView.text = details
                        detailsTextView.setTextIsSelectable(true)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (boundToCountryService) {
            unbindService(countryServiceConnection)
            boundToCountryService = false
        }
    }
}