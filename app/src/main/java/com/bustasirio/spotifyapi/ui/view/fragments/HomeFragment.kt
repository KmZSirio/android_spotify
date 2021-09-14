package com.bustasirio.spotifyapi.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.databinding.FragmentHomeBinding
import com.bustasirio.spotifyapi.ui.viewmodel.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

import android.content.Context
import android.opengl.Visibility
import android.util.Log

import android.widget.Toast
import androidx.core.view.get
import androidx.navigation.findNavController

import com.bustasirio.spotifyapi.ui.view.adapters.TopArtistsAdapter
import com.bustasirio.spotifyapi.ui.view.adapters.TopTracksAdapter
import com.google.android.material.chip.Chip
import java.util.*


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var fragmentHomeBinding: FragmentHomeBinding? = null

    private val homeFragmentViewModel: HomeFragmentViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onDestroyView() {
        fragmentHomeBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)
        fragmentHomeBinding = binding

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

        getPrefs()
        homeFragmentViewModel.fetchTopArtists()
        homeFragmentViewModel.fetchTopTracks("short_term")
        homeFragmentViewModel.fetchTopTracks("medium_term")
        homeFragmentViewModel.fetchTopTracks("long_term")

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
        homeFragmentViewModel.topArtistsResponse.observe(viewLifecycleOwner, {
//            Log.d("tagHomeFragmentResponse", it.artists[2].name)

//            binding.rvTopArtists.layoutManager = LinearLayoutManager(requireContext())
            val adapter = TopArtistsAdapter(it.artists)
            binding.rvTopArtists.adapter = adapter

        })

        // * TopTracksResponse
        // ! TODO Chip to select scope of most played songs, month, 6 months, lifetime
        homeFragmentViewModel.topTracksMonthResponse.observe(viewLifecycleOwner, {
            val adapter = TopTracksAdapter(it.tracks)
            binding.rvTopTracksMonth.adapter = adapter
        })

        homeFragmentViewModel.topTracksSixMonthsResponse.observe(viewLifecycleOwner, {
            val adapter = TopTracksAdapter(it.tracks)
            binding.rvTopTracksSixMonths.adapter = adapter
        })

        homeFragmentViewModel.topTracksLifetimeResponse.observe(viewLifecycleOwner, {
            val adapter = TopTracksAdapter(it.tracks)
            binding.rvTopTracksLifetime.adapter = adapter
        })

        homeFragmentViewModel.responseError.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        // * Save new tokens
        homeFragmentViewModel.responseNewTokens.observe(viewLifecycleOwner, {
//            Log.d("tagHomeActivityResponseNewTokens", it.toString())

            val sharedPrefs = requireContext().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            with(sharedPrefs.edit()) {
                putString(
                    getString(com.bustasirio.spotifyapi.R.string.spotify_access_token),
                    it.accessToken
                )
                putString(
                    getString(com.bustasirio.spotifyapi.R.string.spotify_token_type),
                    it.tokenType
                )
                putBoolean(getString(com.bustasirio.spotifyapi.R.string.spotify_logged), true)
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

        homeFragmentViewModel.authorizationWithToken.value = "$tokenType $accessToken"
        homeFragmentViewModel.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        homeFragmentViewModel.refreshToken.value = refreshToken
    }
}