package fr.hureljeremy.gitea.tp2mobile.ex7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.GpsService
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class Maps : AppCompatActivity() {
    private lateinit var gpsService: GpsService
    private lateinit var map: MapView
    private lateinit var homeButton: android.widget.Button
    private var marker: Marker? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GpsService.LocalBinder
            gpsService = binder.getService()
            setupLocationUpdates()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().apply {
            userAgentValue = packageName
        }

        setContentView(R.layout.activity_maps)

        map = findViewById(R.id.map)
        homeButton = findViewById(R.id.btn_home)
        setupMap()

        homeButton.setOnClickListener {
            finish()
        }

        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            1
        )

        Intent(this, GpsService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }


    }

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Set initial position (Paris, France)
        val startPoint = GeoPoint(48.8566, 2.3522)
        map.controller.apply {
            setZoom(18.0)
            setCenter(startPoint)
        }

        marker = Marker(map).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "My Location"
            position = startPoint
        }
        map.overlays.add(marker)
        map.invalidate()
    }

    private fun setupLocationUpdates() {
        gpsService.setLocationCallback { location ->
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            runOnUiThread {
                if (marker == null) {
                    marker = Marker(map).apply {
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "My Location"
                    }
                    map.overlays.add(marker)
                }
                marker?.apply {
                    position = geoPoint
                    title = "My Location"
                }
                map.controller.apply {
                    animateTo(geoPoint)
                    setZoom(18.0)
                }
                map.invalidate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}