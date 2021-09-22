package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_SIZE
import com.bustasirio.spotifyapi.core.convertDpToPx
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.databinding.FragmentPlaylistBinding
import com.bustasirio.spotifyapi.ui.view.adapters.PlaylistAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.PlaylistViewModel
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private val playlistVM: PlaylistViewModel by viewModels()

    private lateinit var playlistAdapter: PlaylistAdapter

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val playlist = arguments!!.getParcelable<Playlist>(getString(R.string.tracks_url))
        playlistVM.tracksUrl.value = playlist!!.tracks.href

        val binding = FragmentPlaylistBinding.bind(view)
        setupRecyclerView(binding)

        getPrefs()
        playlistVM.fetchPlaylistItems()

//        binding.collapsingPlaylist.contentScrim =
//            resources.getDrawable(R.drawable.gradient_collapsed, resources.newTheme())

        binding.rvPlaylist.overScrollMode = View.OVER_SCROLL_NEVER

        binding.tvTitlePlaylist.text = playlist.name
        binding.tvOwnerPlaylist.text = playlist.owner.display_name

        // * Appbar height depending if the playlist has a description
        if (playlist.description.isNullOrEmpty()) {
            val paramsAppBar = binding.appBarPlaylist.layoutParams
            paramsAppBar.height = convertDpToPx(330, resources)
            binding.appBarPlaylist.layoutParams = paramsAppBar

            val paramsMargin: ViewGroup.MarginLayoutParams =
                binding.llDetailsPlaylist.layoutParams as ViewGroup.MarginLayoutParams
            paramsMargin.bottomMargin = convertDpToPx(10, resources)
            binding.tvDescriptionPlaylist.visibility = View.GONE
        }
        else binding.tvDescriptionPlaylist.text = playlist.description

        if (playlist.images.isNotEmpty()) Picasso.get().load(playlist.images[0].url)
            .into(binding.ivPlaylist)
        else binding.ivPlaylist.setImageResource(R.drawable.playlist_cover)

        binding.ibBackPlaylist.setOnClickListener { removeAnnoyingFrag(requireActivity().supportFragmentManager) }

        // * To hide title from the collapsing bar when extended
        var isShow = true
        var scrollRange = -1
        binding.appBarPlaylist.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                binding.collapsingPlaylist.title = playlist.name
                isShow = true
                binding.toolbarPlaylist.background =
                    resources.getDrawable(R.drawable.gradient_collapsed, resources.newTheme())
            } else if (isShow) {
                binding.collapsingPlaylist.title =
                    " " //careful there should a space between double quote otherwise it wont work
                isShow = false
                binding.toolbarPlaylist.background = null
            }
        })

        playlistAdapter.setOnItemClickListener {
            val url = it.track.preview_url
            Log.d("tagPlaylistFragment", "preview_url: $url")
            if (url != null) {
                mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(url))
                mediaPlayer?.start()
            } else {
                Toast.makeText(
                    requireContext(),
                    "This track cannot be reproduced.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        // * TracksResponse
        playlistVM.tracksResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)

            playlistAdapter.differ.submitList(it.items.toList())
            val totalPages = it.total / QUERY_SIZE + 1
            isLastPage = playlistVM.page == totalPages

            requireActivity().window.statusBarColor =
                requireActivity().getColor(R.color.spotifyBlueGrey)
        })

        playlistVM.loading.observe(viewLifecycleOwner, {
            if (it) showProgressBar(view)
        })

        playlistVM.errorResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT)
                    .show()
            }
        })


        // ! FIXME Send <<it>> to a fun on helpers
        playlistVM.newTokensResponse.observe(viewLifecycleOwner, {
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

    private fun hideProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressPlaylist)
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressPlaylist)
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
                playlistVM.fetchPlaylistItems()
                isScrolling = false
            }
        }
    }

    private fun setupRecyclerView(binding: FragmentPlaylistBinding) {
        playlistAdapter = PlaylistAdapter()
        binding.rvPlaylist.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@PlaylistFragment.scrollListener)
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

        playlistVM.authorizationWithToken.value = "$tokenType $accessToken"
        playlistVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        playlistVM.refreshToken.value = refreshToken
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
    }
}