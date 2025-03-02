package fr.hureljeremy.gitea.tp2mobile.ex9

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import fr.hureljeremy.gitea.tp2mobile.R
import fr.hureljeremy.gitea.tp2mobile.ex8.CountryInfo
import fr.hureljeremy.gitea.tp2mobile.services.CountryService

class CountryInfoFragment : Fragment() {
    private lateinit var countryService: CountryService
    private var boundToCountryService = false
    private lateinit var detailsTextView: TextView
    private lateinit var btnHome: Button
    private var countryCode: String? = null

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
        arguments?.let {
            countryCode = it.getString(ARG_COUNTRY_CODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_country_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupHomeButton()
        bindCountryService()
    }

    private fun setupViews(view: View) {
        detailsTextView = view.findViewById(R.id.countryDetailsTextView)
        btnHome = view.findViewById(R.id.btn_home)
    }

    private fun setupHomeButton() {
        btnHome.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun bindCountryService() {
        activity?.let { activity ->
            Intent(activity, CountryService::class.java).also { intent ->
                activity.bindService(intent, countryServiceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun loadCountryDetails() {
        if (boundToCountryService) {
            countryCode?.let { code ->
                countryService.getCountryDetails(code) { details ->
                    activity?.runOnUiThread {
                        detailsTextView.text = details
                        detailsTextView.setTextIsSelectable(true)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (boundToCountryService) {
            activity?.unbindService(countryServiceConnection)
            boundToCountryService = false
        }
    }

    companion object {
        private const val ARG_COUNTRY_CODE = "countryCode"

        @JvmStatic
        fun newInstance(countryCode: String) =
            CountryInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COUNTRY_CODE, countryCode)
                }
            }
    }
}