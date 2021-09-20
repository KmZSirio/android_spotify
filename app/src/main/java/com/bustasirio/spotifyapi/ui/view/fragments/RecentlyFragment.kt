package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.databinding.FragmentLibraryBinding
import com.bustasirio.spotifyapi.databinding.FragmentRecentlyBinding
import com.bustasirio.spotifyapi.ui.view.adapters.LibraryPlaylistsAdapter
import com.bustasirio.spotifyapi.ui.view.adapters.RecentlyAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.RecentlyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentlyFragment : Fragment() {

    private val recentlyVM: RecentlyViewModel by viewModels()

    private lateinit var recentlyAdapter: RecentlyAdapter

    var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        removeAnnoyingFrag(requireActivity())
        return inflater.inflate(R.layout.fragment_recently, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRecentlyBinding.bind(view)
        setupRecyclerView(binding)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        binding.rvRecently.overScrollMode = View.OVER_SCROLL_NEVER

        getPrefs()
        recentlyVM.fetchRecentlyPlayed()

        // * RecentlyResponse
        recentlyVM.recentlyResponse.observe(viewLifecycleOwner, {
            recentlyAdapter.differ.submitList(it.play_histories.toList())
        })

        recentlyAdapter.setOnItemClickListener {
            var url = it.track.preview_url
            Log.d("tagRecentlyFragment", "preview_url: $url")
            if (url != null) {
                mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(url))
                mediaPlayer?.start()
            } else {
                Toast.makeText(requireContext(), "This track cannot be reproduced.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        recentlyVM.errorResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        recentlyVM.newTokensResponse.observe(viewLifecycleOwner, {

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

    private fun setupRecyclerView(binding: FragmentRecentlyBinding) {
        recentlyAdapter = RecentlyAdapter()
        binding.rvRecently.apply {
            adapter = recentlyAdapter
            layoutManager = LinearLayoutManager(activity)
//            addOnScrollListener(this@RecentlyFragment.scrollListener)
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

        // ! FIXME helper of this, send VM
        recentlyVM.authorizationWithToken.value = "$tokenType $accessToken"
        recentlyVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        recentlyVM.refreshToken.value = refreshToken
    }
}