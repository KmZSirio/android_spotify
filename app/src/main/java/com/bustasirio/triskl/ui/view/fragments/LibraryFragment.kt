package com.bustasirio.triskl.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bustasirio.triskl.R
import com.bustasirio.triskl.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.BitmapFactory
import android.widget.AbsListView
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.core.*
import com.bustasirio.triskl.core.Constants.Companion.QUERY_SIZE
import com.bustasirio.triskl.data.model.User
import com.bustasirio.triskl.ui.view.adapters.LibraryPlaylistsAdapter
import com.bustasirio.triskl.ui.viewmodel.LibraryViewModel
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

        removeAnnoyingFrags(requireActivity().supportFragmentManager)
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLibraryBinding.bind(view)
        setupRecyclerView(binding)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        var user: User? = null
        var noPlaylist: Int? = null

        binding.rvPlaylistsLibrary.overScrollMode = View.OVER_SCROLL_NEVER

        getPrefs()
        libraryVM.safeFetchPlaylists()
        libraryVM.safeFetchUserProfile()

        binding.ibCreateLibrary.setOnClickListener { if (user != null) fragTransCreate(user) }

        binding.ivProfileLibrary.setOnClickListener {
            if (user != null && noPlaylist != null) fragTransProfile(
                user,
                noPlaylist
            )
        }

        // * UserResponse
        libraryVM.userResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    if (!response.data!!.images.isNullOrEmpty()) {
                        Picasso.get().load(response.data.images[0].url).transform(CircleTransformation())
                            .into(binding.ivProfileLibrary)
                    } else {
                        val icon = BitmapFactory.decodeResource(
                            requireContext().resources,
                            R.drawable.no_image
                        )
                        binding.ivProfileLibrary.setImageBitmap(getCroppedBitmap(icon))
                    }
                    user = response.data
                }
                is Resource.Error -> {
                    if (response.message != "403")
                        errorToast(response.message ?: "", requireContext())
                }
                is Resource.Loading -> {}
            }
        })

        libraryAdapter.setOnItemClickListener {
            val isOwner = user!!.id == it.owner.id
            fragTransPlaylist(
                requireActivity(),
                getString(R.string.tracks_url),
                it,
                getString(R.string.owner_boolean),
                isOwner
            )
        }

        // * PlaylistsResponse
        libraryVM.playlistsResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar(view)
                    libraryAdapter.differ.submitList(response.data!!.playlists.toList())
                    val totalPages = response.data.total / QUERY_SIZE + 1
                    isLastPage = libraryVM.page == totalPages

                    noPlaylist = response.data.total

                    if (reloading) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(200)
                            binding.rvPlaylistsLibrary.layoutManager?.scrollToPosition(0)
                        }
                        reloading = false
                    }
                }
                is Resource.Error -> {
                    hideProgressBar(view)
                    if (response.message == "403") {
                        showToast(requireContext(), getString(R.string.told_you), true)
                        logOut(requireActivity(), requireContext())
                    } else {
                        errorToast(response.message ?: "", requireContext())
                    }
                }
                is Resource.Loading -> { showProgressBar(view) }
            }

        })

        libraryVM.newTokensResponse.observe(
            viewLifecycleOwner,
            { saveTokens(it, requireContext()) })

        // * Listening response from CreateFragment *-*
        requireActivity().supportFragmentManager.setFragmentResultListener(
            getString(R.string.create_fragment),
            viewLifecycleOwner
        ) { _, bundle ->

            val result = bundle.getString(getString(R.string.create_fragment))
            if (result == "reload") {
                reloading = true
                libraryVM.playlists = null
                libraryVM.page = 0

//              getPrefs()
                libraryVM.safeFetchPlaylists()
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
                libraryVM.safeFetchPlaylists()
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
        libraryVM.refreshToken.value = refreshToken
    }
}