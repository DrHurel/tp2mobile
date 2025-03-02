package fr.hureljeremy.gitea.tp2mobile.ex8

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.CountryService
import fr.hureljeremy.gitea.tp2mobile.services.NavigationService

class CountriesList : AppCompatActivity() {

    private lateinit var countryService: CountryService
    private lateinit var navigationService: NavigationService
    private var boundToCountryService = false
    private var boundToNavigationService = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CountryAdapter

    private val countryServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CountryService.LocalBinder
            countryService = binder.getService()
            boundToCountryService = true
            updateCountriesList()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundToCountryService = false
        }
    }

    private val navigationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()
            boundToNavigationService = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundToNavigationService = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_countries_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.countriesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter = CountryAdapter { countryCode ->
            if (boundToNavigationService) {
                navigationService.navigate(this, "country-info", Bundle().apply {
                    putString("countryCode", countryCode)
                })
            }
        }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_home).setOnClickListener {
            if (boundToNavigationService) {
                navigationService.navigate(this, "home")
            }
        }


        Intent(this, CountryService::class.java).also { intent ->
            bindService(intent, countryServiceConnection, Context.BIND_AUTO_CREATE)
        }
        Intent(this, NavigationService::class.java).also { intent ->
            bindService(intent, navigationServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    private fun updateCountriesList() {
        if (boundToCountryService) {
            countryService.getCountries { countries ->
                runOnUiThread {
                    adapter.submitList(countries)
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
        if (boundToNavigationService) {
            unbindService(navigationServiceConnection)
            boundToNavigationService = false
        }
    }
}

