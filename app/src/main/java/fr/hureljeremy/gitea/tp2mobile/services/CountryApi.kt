package fr.hureljeremy.gitea.tp2mobile.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CountryApi {
    @GET("all")
    fun getAllCountries(): Call<List<CountryResponse>>

    @GET("name/{country}")
    fun getCountryDetails(@Path("country") country: String): Call<List<CountryResponse>>
}

data class CountryResponse(
    val name: Name,
    val capital: List<String>?,
    val region: String,
    val population: Int,
    val flag: String
)

data class Name(
    val common: String
)