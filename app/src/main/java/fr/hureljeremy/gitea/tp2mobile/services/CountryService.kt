package fr.hureljeremy.gitea.tp2mobile.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




class CountryService : Service() {

    private val binder = LocalBinder()
    private val api: CountryApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://restcountries.com/v3.1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(CountryApi::class.java)
    }

    inner class LocalBinder : Binder() {
        fun getService(): CountryService = this@CountryService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**
     * Fetches the list of country names asynchronously.
     */
    fun getCountries(callback: (List<CountryResponse>) -> Unit) {
        api.getAllCountries().enqueue(object : Callback<List<CountryResponse>> {
            override fun onResponse(
                call: Call<List<CountryResponse>>,
                response: Response<List<CountryResponse>>
            ) {
                if (response.isSuccessful) {
                    callback(response.body() ?: emptyList())
                } else {
                    Log.e("CountryService", "Failed to fetch countries")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<CountryResponse>>, t: Throwable) {
                Log.e("CountryService", "Error: ${t.message}")
                callback(emptyList())
            }
        })
    }

    /**
     * Fetches country details asynchronously.
     */
    fun getCountryDetails(country: String, callback: (String) -> Unit) {
        api.getCountryDetails(country).enqueue(object : Callback<List<CountryResponse>> {
            override fun onResponse(
                call: Call<List<CountryResponse>>,
                response: Response<List<CountryResponse>>
            ) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val countryInfo = response.body()?.first()
                    val details = """
                        Name: ${countryInfo?.name?.common}
                        Capital: ${countryInfo?.capital?.joinToString(", ")}
                        Region: ${countryInfo?.region}
                        Population: ${countryInfo?.population}
                        Flag: ${countryInfo?.flag}
                    """.trimIndent()
                    callback(details)
                } else {
                    Log.e("CountryService", "Failed to fetch country details")
                    callback("No details found for $country")
                }
            }

            override fun onFailure(call: Call<List<CountryResponse>>, t: Throwable) {
                Log.e("CountryService", "Error: ${t.message}")
                callback("Error fetching country details")
            }
        })
    }
}
