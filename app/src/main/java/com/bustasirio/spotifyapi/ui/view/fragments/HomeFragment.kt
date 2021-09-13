package com.bustasirio.spotifyapi.ui.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.databinding.FragmentHomeBinding
import com.bustasirio.spotifyapi.ui.viewmodel.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context

import android.content.IntentFilter
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bustasirio.spotifyapi.data.model.TopArtistsModel
import com.bustasirio.spotifyapi.ui.view.adapters.TopArtistsAdapter


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

        getPrefs()
        homeFragmentViewModel.fetchTopArtists()

        // * viewLifecycleOwner instead of this, if you are on a fragment
        homeFragmentViewModel.response.observe(viewLifecycleOwner, {
            Log.d("tagHomeActivityResponse", it.artists[2].name)

//            binding.rvTopArtists.layoutManager = LinearLayoutManager(requireContext())
            val adapter = TopArtistsAdapter(it.artists)
            binding.rvTopArtists.adapter = adapter

        })

        homeFragmentViewModel.responseError.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT).show()
            }
        })

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

    private fun getPrefs() {
        val sharedPrefs =
            requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
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