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
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.core.CircleTransformation
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_LIBRARY_SIZE
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.data.model.User
import com.bustasirio.spotifyapi.ui.view.adapters.LibraryPlaylistsAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.LibraryFragmentViewModel
import com.squareup.picasso.Picasso

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private val libraryFragmentVM: LibraryFragmentViewModel by viewModels()

    private lateinit var libraryAdapter: LibraryPlaylistsAdapter

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
        setupRecyclerView(binding)
//        layoutManager = LinearLayoutManager(requireContext())
//        binding.rvPlaylistsLibrary.layoutManager = layoutManager
//        binding.rvPlaylistsLibrary.addOnScrollListener(this@LibraryFragment.scrollListener)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack);

        var user: User? = null
        var noPlaylist: Int? = null

        binding.rvPlaylistsLibrary.overScrollMode = View.OVER_SCROLL_NEVER

        getPrefs()
        libraryFragmentVM.fetchCurrentUserPlaylists()
        libraryFragmentVM.fetchCurrentUserProfile()


        binding.ivProfileLibrary.setOnClickListener {

            if (user != null && noPlaylist != null) {
                fragTransProfile(user, noPlaylist)
            }
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
            user = it
        })

        libraryAdapter.setOnItemClickListener {
            fragTransPlaylist(it)
        }

        // * PlaylistsResponse
        libraryFragmentVM.playlistsResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)
            libraryAdapter.differ.submitList(it.playlists.toList())
            val totalPages = it.total / QUERY_LIBRARY_SIZE + 1
            isLastPage = libraryFragmentVM.page == totalPages

            noPlaylist = it.total
        })

        libraryFragmentVM.loading.observe(viewLifecycleOwner, {
            if (it) showProgressBar(view)
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

    private fun hideProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressLibrary)
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressLibrary)
        progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_LIBRARY_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                libraryFragmentVM.fetchCurrentUserPlaylists()
                isScrolling = false
            }
        }
    }

    private fun setupRecyclerView(binding: FragmentLibraryBinding) {
        libraryAdapter = LibraryPlaylistsAdapter()
        binding.rvPlaylistsLibrary.apply {
            adapter = libraryAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@LibraryFragment.scrollListener)
        }
    }

    private fun fragTransPlaylist(playlist: Playlist) {
        val fragment = PlaylistFragment()

        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.tracks_url), playlist)
        fragment.arguments = bundle
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment_activity_lobby, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun fragTransProfile(user: User?, noPlaylists: Int?) {
        val fragment = ProfileFragment()

        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.user_object), user)
        bundle.putInt(getString(R.string.no_playlists), noPlaylists!!)
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