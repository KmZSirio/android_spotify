package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.core.errorToast
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.databinding.FragmentPlaylistBinding
import com.bustasirio.spotifyapi.databinding.FragmentSavedBinding
import com.bustasirio.spotifyapi.ui.view.adapters.PlaylistAdapter
import com.bustasirio.spotifyapi.ui.view.adapters.SavedSongsAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.SavedViewModel
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedFragment : Fragment() {

    private var type = ""

    private val savedVM: SavedViewModel by viewModels()

    private lateinit var savedSongsAdapter: SavedSongsAdapter

    private var mediaPlayer: MediaPlayer? = null

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        type = arguments?.getString(getString(R.string.arg_saved_from_home)) ?: ""

        val binding = FragmentSavedBinding.bind(view)
        binding.rvSaved.overScrollMode = View.OVER_SCROLL_NEVER

        var title = ""

        getPrefs()
        when (type) {
            "songs" -> {
                title = getString(R.string.liked_songs)
                binding.tvTitleSaved.text = title
                binding.ivSaved.setImageResource(R.drawable.liked_songs)
                savedVM.fetchSavedSongs()
                setupRWSongs(binding)
            }
            "episodes" -> {
                title = getString(R.string.your_episodes)
                binding.tvTitleSaved.text = title
                binding.ivSaved.setImageResource(R.drawable.your_episodes)
//                savedVM.fetchSavedEpisodes()
                setupRWEpisodes(binding)
            }
            "shows" -> {
                title = getString(R.string.saved_shows)
                binding.tvTitleSaved.text = title
                binding.ivSaved.setImageResource(R.drawable.saved_shows)
//                savedVM.fetchSavedShows()
                setupRWShows(binding)
            }
        }

        binding.ibBackSaved.setOnClickListener { removeAnnoyingFrag(requireActivity().supportFragmentManager) }

        // * To hide title from the collapsing bar when extended
        var isShow = true
        var scrollRange = -1
        binding.appBarSaved.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                binding.collapsingSaved.title = title
                isShow = true
                binding.toolbarSaved.background =
                    resources.getDrawable(R.drawable.gradient_collapsed, resources.newTheme())
            } else if (isShow) {
                binding.collapsingSaved.title =
                    " " //careful there should a space between double quote otherwise it wont work
                isShow = false
                binding.toolbarSaved.background = null
            }
        })

        // * SONGS
        savedVM.songsResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)

            savedSongsAdapter.differ.submitList(it.savedTracks.toList())
            val totalPages = it.total / Constants.QUERY_SIZE + 1
            isLastPage = savedVM.page == totalPages

            requireActivity().window.statusBarColor =
                requireActivity().getColor(R.color.spotifyBlueGrey)
        })

        savedSongsAdapter.setOnItemClickListener { reproduce(it.track.preview_url) }

        savedVM.loading.observe(viewLifecycleOwner, { if (it) showProgressBar(view) })

        savedVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        savedVM.newTokensResponse.observe(viewLifecycleOwner, {
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

    private fun setupRWSongs(binding: FragmentSavedBinding) {
        savedSongsAdapter = SavedSongsAdapter()
        binding.rvSaved.apply {
            adapter = savedSongsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SavedFragment.scrollListener)
        }
    }

    private fun setupRWEpisodes(binding: FragmentSavedBinding) {
//        episodesAdapter = EpisodesAdapter()
//        binding.rvSaved.apply {
//            adapter = episodesAdapter
//            layoutManager = LinearLayoutManager(activity)
//            addOnScrollListener(this@SavedFragment.scrollListener)
//        }
    }

    private fun setupRWShows(binding: FragmentSavedBinding) {
//        showsAdapter = ShowsAdapter()
//        binding.rvSaved.apply {
//            adapter = showsAdapter
//            layoutManager = LinearLayoutManager(activity)
//            addOnScrollListener(this@SavedFragment.scrollListener)
//        }
    }

    private fun reproduce(url: String?) {
        if (url != null) {
            mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(url))
            mediaPlayer?.start()
        } else {
            Toast.makeText(
                requireContext(),
                "This track cannot be reproduced.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hideProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressSaved)
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressSaved)
        progressBar.visibility = View.VISIBLE
        isLoading = true
    }

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
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                when (type) {
                    "songs" -> savedVM.fetchSavedShows()
                    "episodes" -> savedVM.fetchSavedEpisodes()
                    "shows" -> savedVM.fetchSavedShows()
                }
                isScrolling = false
            }
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

        savedVM.authorizationWithToken.value = "$tokenType $accessToken"
        savedVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        savedVM.refreshToken.value = refreshToken
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
    }
}