package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.errorToast
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.core.saveTokens
import com.bustasirio.spotifyapi.databinding.FragmentSettingsBinding
import com.bustasirio.spotifyapi.ui.view.activities.MainActivity
import com.bustasirio.spotifyapi.ui.view.adapters.CountrySpinnerAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val settingsVM: SettingsViewModel by viewModels()

    private var country = ""
    private var countryPos = 0
    private var language = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSettingsBinding.bind(view)
        binding.spCountrySettings.overScrollMode = View.OVER_SCROLL_NEVER

        getPrefs()
        settingsVM.fetchMarkets()

        if (language == "en_US") binding.chipGroupSettings.check(R.id.chipFirstSettings)
        if (language == "es_MX") binding.chipGroupSettings.check(R.id.chipSecondSettings)

        binding.chipGroupSettings.setOnCheckedChangeListener { chipGroup, _ ->
            when (chipGroup.checkedChipId) {
                binding.chipFirstSettings.id -> saveLanguage(requireContext(), "en_US")
                binding.chipSecondSettings.id -> saveLanguage(requireContext(), "es_MX")
            }
        }

        binding.toolbarSettings.setNavigationOnClickListener { removeAnnoyingFrag(requireActivity().supportFragmentManager) }

        binding.logOutSettings.setOnClickListener { logOut() }

        settingsVM.marketsResponse.observe(
            viewLifecycleOwner,
            { setupSpinner(it.markets, binding) })

        settingsVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        settingsVM.newTokensResponse.observe(
            viewLifecycleOwner,
            { saveTokens(it, requireContext()) })
    }

    private fun setupSpinner(markets: MutableList<String>, binding: FragmentSettingsBinding) {
        markets.add(0, "")
        val adapter = CountrySpinnerAdapter(requireContext(), markets)
        binding.spCountrySettings.adapter = adapter
        binding.spCountrySettings.setSelection(countryPos)

        binding.spCountrySettings.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selected = parent!!.getItemAtPosition(position).toString()
                    if (selected != country) {
                        saveCountry(requireContext(), position, selected)
                        countryPos = position
                        country = selected
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun saveCountry(context: Context, pos: Int, country: String) {
        val sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        with(sharedPrefs.edit()) {
            putInt(
                context.getString(R.string.spotify_country_pos),
                pos
            )
            putString(
                context.getString(R.string.spotify_country),
                country
            )
            apply()
        }
    }

    private fun saveLanguage(context: Context, language: String) {
        val sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        with(sharedPrefs.edit()) {
            putString(
                context.getString(R.string.spotify_language),
                language
            )
            apply()
        }
    }

    private fun getPrefs() {
        val sharedPrefs =
            requireContext().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val tokenType: String? = sharedPrefs.getString(getString(R.string.spotify_token_type), "")
        val accessToken: String? =
            sharedPrefs.getString(getString(R.string.spotify_access_token), "")
        val refreshToken: String? =
            sharedPrefs.getString(getString(R.string.spotify_refresh_token), "")

        country = sharedPrefs.getString(getString(R.string.spotify_country), "") ?: ""
        countryPos = sharedPrefs.getInt(getString(R.string.spotify_country_pos), 0)

        language = sharedPrefs.getString(getString(R.string.spotify_language), "en_US") ?: "en_US"

        settingsVM.authorizationWithToken.value = "$tokenType $accessToken"
        settingsVM.auth.value =
            resources.getString(R.string.esl)
        settingsVM.refreshToken.value = refreshToken
    }

    private fun logOut() {
        requireActivity().supportFragmentManager.popBackStack()

        val sharedPrefs = requireContext().getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        with(sharedPrefs.edit()) {
            putString(getString(R.string.spotify_access_token), "")
            putString(getString(R.string.spotify_token_type), "")
            putString(getString(R.string.spotify_refresh_token), "")
            putBoolean(getString(R.string.spotify_logged), false)
            putString(getString(R.string.spotify_country), "")
            putInt(getString(R.string.spotify_country_pos), 0)
            putString(getString(R.string.spotify_language), "en_US")
            apply()
        }

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}