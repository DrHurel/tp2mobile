package fr.hureljeremy.gitea.tp2mobile.ex8

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.services.CountryResponse

class CountryAdapter(
    private val onCountryClick: (String) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {
    private var countries: List<CountryResponse> = emptyList()

    fun submitList(newList: List<CountryResponse>) {
        countries = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = countries[position]
        holder.bind(country)
        holder.itemView.setOnClickListener { onCountryClick(country.name.common) }
    }

    override fun getItemCount(): Int = countries.size


    class CountryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val countryName: TextView = view.findViewById(R.id.country_name)
        private val countryCode: TextView = view.findViewById(R.id.country_code)

        fun bind(country: CountryResponse) {
            countryName.text = country.name.common
            countryCode.text = country.flag
        }
    }
}