package fr.hureljeremy.gitea.tp2mobile.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class NavigationService : Service() {

    private val binder = LocalBinder()
    private val destinations = mutableMapOf<String, Class<*>>()
    private val pageIntent = mutableMapOf<String, Intent>()
    private var currentDestination: String? = null

    inner class LocalBinder : Binder() {
        fun getService(): NavigationService = this@NavigationService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    fun navigate(context: Context, page: String, apply: Bundle? = null) {
        if (currentDestination == page) {
            Log.d("NavigationService", "Already on $page")
            return
        }
        val destination = destinations[page]
        if (destination != null) {
            this.currentDestination = page
            val intent = pageIntent[page] ?: Intent(context, destination)
            if (pageIntent[page] == null) {
                pageIntent[page] = intent
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (apply != null) {
                intent.putExtras(apply)
            }
            context.startActivity(intent)
            Log.d("NavigationService", "Navigating to $page")
        } else {
            Log.e("NavigationService", "Destination $page not registered")
        }
    }

    fun registerDestination(page: String, ui: Class<*>) {
        destinations[page] = ui
        Log.d("NavigationService", "Registered destination: $page -> $ui")
    }

    fun unregisterDestination(page: String) {
        destinations.remove(page)
        Log.d("NavigationService", "Unregistered destination: $page")
    }

    fun getCurrentDestination(): String? {
        return currentDestination
    }

    fun clearDestinations() {
        destinations.clear()
        Log.d("NavigationService", "Cleared all destinations")
    }

    fun getDestinations(): List<String> {
        return destinations.keys.toList()
    }

}
