package com.bustasirio.spotifyapi.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.databinding.FragmentHomeBinding
import com.bustasirio.spotifyapi.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

import android.content.Context
import android.content.Intent

import android.widget.Toast
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.ui.view.activities.MainActivity

import com.bustasirio.spotifyapi.ui.view.adapters.TopArtistsAdapter
import com.bustasirio.spotifyapi.ui.view.adapters.TopTracksAdapter
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeVM: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        removeAnnoyingFrag(requireActivity().supportFragmentManager)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        binding.tvWelcomeMessageHome.text = setWelcomeMessage()

        binding.chipGroupHome.check(R.id.chipFirstHome)
        binding.rvTopTracksMonth.visibility = View.VISIBLE
        binding.rvTopTracksSixMonths.visibility = View.GONE
        binding.rvTopTracksLifetime.visibility = View.GONE

        binding.svHome.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvTopTracksMonth.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvTopTracksSixMonths.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvTopTracksLifetime.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvTopArtists.overScrollMode = View.OVER_SCROLL_NEVER

        if (isLogged()) {
            getPrefs()
            homeVM.fetchTopArtists()
            homeVM.fetchTopTracks("short_term")
            homeVM.fetchTopTracks("medium_term")
            homeVM.fetchTopTracks("long_term")
        }

        binding.ibLogOutHome.setOnClickListener {

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
                apply()
            }

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // * Chips related
        binding.chipGroupHome.setOnCheckedChangeListener { chipGroup, id ->

// *           Log.d("tagHomeFragment", "test: ${chipGroup.findViewById<Chip>(id).text}")
            if (binding.chipFirstHome.id == chipGroup.checkedChipId) {
                binding.rvTopTracksMonth.visibility = View.VISIBLE
                binding.rvTopTracksSixMonths.visibility = View.GONE
                binding.rvTopTracksLifetime.visibility = View.GONE
            }
            if (binding.chipSecondHome.id == chipGroup.checkedChipId) {
                binding.rvTopTracksMonth.visibility = View.GONE
                binding.rvTopTracksSixMonths.visibility = View.VISIBLE
                binding.rvTopTracksLifetime.visibility = View.GONE
            }
            if (binding.chipThirdHome.id == chipGroup.checkedChipId) {
                binding.rvTopTracksMonth.visibility = View.GONE
                binding.rvTopTracksSixMonths.visibility = View.GONE
                binding.rvTopTracksLifetime.visibility = View.VISIBLE
            }
        }

        // * viewLifecycleOwner instead of this, if you are on a fragment
        // * TopArtistsResponse
        homeVM.topArtistsResponse.observe(viewLifecycleOwner, {
            val adapter = TopArtistsAdapter(it.artists)
            binding.rvTopArtists.adapter = adapter
        })

        // * TopTracksResponse
        homeVM.topTracksMonthResponse.observe(viewLifecycleOwner, {
            val adapter = TopTracksAdapter(it.tracks)
            binding.rvTopTracksMonth.adapter = adapter
        })

        homeVM.topTracksSixMonthsResponse.observe(viewLifecycleOwner, {
            val adapter = TopTracksAdapter(it.tracks)
            binding.rvTopTracksSixMonths.adapter = adapter
        })

        homeVM.topTracksLifetimeResponse.observe(viewLifecycleOwner, {
            val adapter = TopTracksAdapter(it.tracks)
            binding.rvTopTracksLifetime.adapter = adapter
        })

        // ! FIXME reduce code repetition!
        // ! FIXME one error will create multiple toasts, one per endpoint call
        homeVM.errorResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        // ! FIXME reduce code repetition!
        // * Save new tokens
        homeVM.newTokensResponse.observe(viewLifecycleOwner, {
//            Log.d("tagHomeActivityResponseNewTokens", it.toString())

            val sharedPrefs = requireContext().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            with(sharedPrefs.edit()) {
                putString(
                    getString(R.string.spotify_access_token),
                    it.accessToken
                )
                putString(
                    getString(R.string.spotify_token_type),
                    it.tokenType
                )
                putBoolean(getString(R.string.spotify_logged), true)
                apply()
            }
        })

    }

    private fun setWelcomeMessage(): String {
        val currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour in 6..11) return "Good morning"
        if (currentHour in 12..18) return "Good afternoon"
        return "Good evening"
    }

    // ! FIXME reduce code repetition!
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

        homeVM.authorizationWithToken.value = "$tokenType $accessToken"
        homeVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        homeVM.refreshToken.value = refreshToken
    }

    private fun isLogged(): Boolean {
        val sharedPrefs = requireContext().getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        return sharedPrefs.getBoolean(getString(R.string.spotify_logged), false)
    }
}