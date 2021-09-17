package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.getCroppedBitmap
import com.bustasirio.spotifyapi.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Transformations
import com.bustasirio.spotifyapi.core.CircleTransformation
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.ui.view.adapters.LibraryPlaylistsAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.LibraryFragmentViewModel
import com.squareup.picasso.Picasso

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private val libraryFragmentVM: LibraryFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        removeAnnoyingFrag(requireActivity())
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLibraryBinding.bind(view)

        binding.rvPlaylistsLibrary.overScrollMode = View.OVER_SCROLL_NEVER

//        binding.rvPlaylistsLibrary.
//        libraryPlaylistsAdapter.tracksUrl.observe(viewLifecycleOwner, {
//            Log.d("tagLibraryFragment", it)
//        })

        getPrefs()
        libraryFragmentVM.fetchCurrentUserPlaylists()
        libraryFragmentVM.fetchCurrentUserProfile()

        binding.ivProfileLibrary.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment_activity_lobby, ProfileFragment())
            transaction.disallowAddToBackStack()
            transaction.commit()
        }

        // * UserResponse
        libraryFragmentVM.userResponse.observe(viewLifecycleOwner, {
            if (!it.images.isNullOrEmpty()) {
                Picasso.get().load(it.images[0].url).transform(CircleTransformation())
                    .into(binding.ivProfileLibrary)
            } else {
                val icon = BitmapFactory.decodeResource(
                    requireContext().resources,
                    R.drawable.no_image
                )
                binding.ivProfileLibrary.setImageBitmap(getCroppedBitmap(icon))
            }
        })

        // * PlaylistsResponse
        libraryFragmentVM.playlistsResponse.observe(viewLifecycleOwner, {
            val adapter = LibraryPlaylistsAdapter(it.playlists)
            binding.rvPlaylistsLibrary.adapter = adapter

            adapter.itemPosition.observe(viewLifecycleOwner, { position -> fragTransaction(it.playlists[position]) })
        })

        libraryFragmentVM.errorResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        libraryFragmentVM.newTokensResponse.observe(viewLifecycleOwner, {
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

    private fun fragTransaction(playlist: Playlist) {
        val fragment = PlaylistFragment()

        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.tracks_url), playlist)
        fragment.arguments = bundle
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment_activity_lobby, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
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

        libraryFragmentVM.authorizationWithToken.value = "$tokenType $accessToken"
        libraryFragmentVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        libraryFragmentVM.refreshToken.value = refreshToken
    }
}