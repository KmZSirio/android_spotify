package com.bustasirio.triskl.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bustasirio.triskl.R
import com.bustasirio.triskl.databinding.FragmentHomeBinding
import com.bustasirio.triskl.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

import android.content.Context
import com.bustasirio.triskl.core.*


import com.bustasirio.triskl.ui.view.adapters.TopArtistsAdapter
import com.bustasirio.triskl.ui.view.adapters.TopTracksAdapter
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeVM: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        removeAnnoyingFrags(requireActivity().supportFragmentManager)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        var shortEmpty = false
        var mediumEmpty = false
        var longEmpty = false

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
            homeVM.safeFetchTopArtists()
            homeVM.safeFetchTopTracks("short_term")
            homeVM.safeFetchTopTracks("medium_term")
            homeVM.safeFetchTopTracks("long_term")
        }

        binding.cvSongsHome.setOnClickListener { openFrag("songs") }
        binding.cvEpisodesHome.setOnClickListener { openFrag("episodes") }
        binding.cvShowsHome.setOnClickListener { openFrag("shows") }

        binding.ibHistoryLibrary.setOnClickListener {
            replaceFrag(
                requireActivity(),
                RecentlyFragment()
            )
        }
        binding.ibSettingsHome.setOnClickListener {
            replaceFrag(
                requireActivity(),
                SettingsFragment()
            )
        }

        // * Chips related
        binding.chipGroupHome.setOnCheckedChangeListener { chipGroup, _ ->

            when (chipGroup.checkedChipId) {
                binding.chipFirstHome.id -> {
                    binding.rvTopTracksMonth.visibility = View.VISIBLE
                    binding.rvTopTracksSixMonths.visibility = View.GONE
                    binding.rvTopTracksLifetime.visibility = View.GONE
                    if (shortEmpty) binding.ivEmptyTracks.visibility = View.VISIBLE
                    else binding.ivEmptyTracks.visibility = View.INVISIBLE
                }
                binding.chipSecondHome.id -> {
                    binding.rvTopTracksMonth.visibility = View.GONE
                    binding.rvTopTracksSixMonths.visibility = View.VISIBLE
                    binding.rvTopTracksLifetime.visibility = View.GONE
                    if (mediumEmpty) binding.ivEmptyTracks.visibility = View.VISIBLE
                    else binding.ivEmptyTracks.visibility = View.INVISIBLE
                }
                binding.chipThirdHome.id -> {
                    binding.rvTopTracksMonth.visibility = View.GONE
                    binding.rvTopTracksSixMonths.visibility = View.GONE
                    binding.rvTopTracksLifetime.visibility = View.VISIBLE
                    if (longEmpty) binding.ivEmptyTracks.visibility = View.VISIBLE
                    else binding.ivEmptyTracks.visibility = View.INVISIBLE
                }
            }
        }

        // * viewLifecycleOwner instead of this, if you are on a fragment
        // * TopArtistsResponse
        homeVM.artistsResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    val adapter = TopArtistsAdapter(response.data!!.artists)
                    binding.rvTopArtists.adapter = adapter
                    if (response.data.artists.isEmpty()) binding.ivEmptyArtists.visibility =
                        View.VISIBLE
                }
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
            }
        })

        // * TopTracksResponse
        homeVM.tracksMonthResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    val adapter = TopTracksAdapter(response.data!!.tracks)
                    binding.rvTopTracksMonth.adapter = adapter
                    if (response.data.tracks.isEmpty()) {
                        shortEmpty = true
                        binding.ivEmptyTracks.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
            }
        })

        homeVM.tracksSixMonthsResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    val adapter = TopTracksAdapter(response.data!!.tracks)
                    binding.rvTopTracksSixMonths.adapter = adapter
                    if (response.data.tracks.isEmpty()) mediumEmpty = true
                }
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
            }
        })

        homeVM.tracksLifetimeResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    val adapter = TopTracksAdapter(response.data!!.tracks)
                    binding.rvTopTracksLifetime.adapter = adapter
                    if (response.data.tracks.isEmpty()) longEmpty = true
                }
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
            }
        })

        homeVM.errorResponse.observe(viewLifecycleOwner, {
            errorToast(it, requireContext())
        })

        // * Save new tokens
        homeVM.newTokensResponse.observe(viewLifecycleOwner, { saveTokens(it, requireContext()) })
    }

    private fun openFrag(type: String) =
        fragTransSaved(requireActivity(), getString(R.string.arg_saved_type), type)

    private fun setWelcomeMessage(): String {
        val currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour in 6..11) return getString(R.string.good_morning)
        if (currentHour in 12..18) return getString(R.string.good_afternoon)
        return getString(R.string.good_evening)
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

        homeVM.authorizationWithToken.value = "$tokenType $accessToken"
        homeVM.auth.value =
            resources.getString(R.string.esl)
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