package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.core.*
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_SIZE
import com.bustasirio.spotifyapi.data.model.User
import com.bustasirio.spotifyapi.ui.view.adapters.LibraryPlaylistsAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.LibraryViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private val libraryVM: LibraryViewModel by viewModels()

    private lateinit var libraryAdapter: LibraryPlaylistsAdapter

    private var reloading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        removeAnnoyingFrag(requireActivity().supportFragmentManager)
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("tagLibraryFragment", "onViewCreated")

        val binding = FragmentLibraryBinding.bind(view)
        setupRecyclerView(binding)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        var user: User? = null
        var noPlaylist: Int? = null

        binding.rvPlaylistsLibrary.overScrollMode = View.OVER_SCROLL_NEVER

        getPrefs()
        libraryVM.fetchCurrentUserPlaylists()
        libraryVM.fetchCurrentUserProfile()

        binding.ibHistoryLibrary.setOnClickListener {
            replaceFrag(
                requireActivity(),
                RecentlyFragment()
            )
        }
        binding.ibCreateLibrary.setOnClickListener { if (user != null) fragTransCreate(user) }

        binding.ivProfileLibrary.setOnClickListener {
            if (user != null && noPlaylist != null) fragTransProfile(
                user,
                noPlaylist
            )
        }

        // * UserResponse
        libraryVM.userResponse.observe(viewLifecycleOwner, {
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
            fragTransPlaylist(
                requireActivity(),
                getString(R.string.tracks_url),
                it
            )
        }

        // * PlaylistsResponse
        libraryVM.playlistsResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)
            libraryAdapter.differ.submitList(it.playlists.toList())
            val totalPages = it.total / QUERY_SIZE + 1
            isLastPage = libraryVM.page == totalPages

            noPlaylist = it.total

            if (reloading) {
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(200)
                    binding.rvPlaylistsLibrary.layoutManager?.scrollToPosition(0)
                }
                reloading = false
            }
        })

        libraryVM.loading.observe(viewLifecycleOwner, { if (it) showProgressBar(view) })

        libraryVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        libraryVM.newTokensResponse.observe(viewLifecycleOwner, {

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

        requireActivity().supportFragmentManager.setFragmentResultListener(getString(R.string.createfragment), viewLifecycleOwner) { _, bundle ->

            val result = bundle.getString(getString(R.string.createfragment))
            if (result == "reload") {
                reloading = true
                libraryVM.playlists = null
                libraryVM.page = 0

//              getPrefs()
                libraryVM.fetchCurrentUserPlaylists()
            }
        }
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

    private val scrollListener = object : RecyclerView.OnScrollListener() {
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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                libraryVM.fetchCurrentUserPlaylists()
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

    private fun fragTransProfile(user: User?, noPlaylists: Int?) {
        val fragment = ProfileFragment()

        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.user_object), user)
        bundle.putInt(getString(R.string.no_playlists), noPlaylists!!)
        fragment.arguments = bundle

        replaceFrag(requireActivity(), fragment)
    }

    private fun fragTransCreate(user: User?) {
        val fragment = CreateFragment()

        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.user_object), user)
        fragment.arguments = bundle

        replaceFrag(requireActivity(), fragment)
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

        libraryVM.authorizationWithToken.value = "$tokenType $accessToken"
        libraryVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        libraryVM.refreshToken.value = refreshToken
    }
}