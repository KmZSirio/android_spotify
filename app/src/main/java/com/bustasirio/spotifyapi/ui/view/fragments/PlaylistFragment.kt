package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
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
import com.bustasirio.spotifyapi.core.*
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_SIZE
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
        val playlist = arguments!!.getParcelable<Playlist>(getString(R.string.tracks_url))!!
        val isOwner = arguments.getBoolean(getString(R.string.owner_boolean))

        playlistVM.tracksUrl.value = playlist.tracks.href

        val binding = FragmentPlaylistBinding.bind(view)
        setupRecyclerView(binding)

        if (isOwner) binding.ibMenuPlaylist.visibility = View.VISIBLE

        requireActivity().window.statusBarColor =
            requireActivity().getColor(R.color.spotifyBlueGrey)

        getPrefs()
        playlistVM.fetchPlaylistItems()

        binding.fabPlaylist.setColorFilter(R.color.black)
        binding.rvPlaylist.overScrollMode = View.OVER_SCROLL_NEVER

        binding.tvTitlePlaylist.text = playlist.name
        binding.tvOwnerPlaylist.text = playlist.owner.display_name

        // * Appbar height depending if the playlist has a description
        if (playlist.description.isNullOrEmpty()) descriptionIsEmpty(binding)
        else binding.tvDescriptionPlaylist.text = playlist.description

        if (playlist.images.isNotEmpty()) Picasso.get().load(playlist.images[0].url)
            .into(binding.ivPlaylist)
        else binding.ivPlaylist.setImageResource(R.drawable.playlist_cover)


        binding.ibBackPlaylist.setOnClickListener { removeAnnoyingFrag(requireActivity().supportFragmentManager) }

        // * Edit playlist
        binding.ibMenuPlaylist.setOnClickListener {

            val fragment = EditFragment()

            val bundle = Bundle()
            bundle.putParcelable(getString(R.string.arg_playlist_to_edit), playlist)
            fragment.arguments = bundle
            addFrag(requireActivity(), fragment)
        }

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
            reproduce(
                requireContext(),
                getString(R.string.reproduce_toast),
                it.track.preview_url
            )
        }
        playlistAdapter.setOnMenuClickListener {
            showBottomSheet(
                requireActivity(),
                getString(R.string.arg_bottom_sheet_track),
                it.track
            )
        }

        // * TracksResponse
        playlistVM.tracksResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)

            playlistAdapter.differ.submitList(it.items.toList())
            val totalPages = it.total / QUERY_SIZE + 1
            isLastPage = playlistVM.page == totalPages
        })

        playlistVM.loading.observe(viewLifecycleOwner, { if (it) showProgressBar(view) })

        playlistVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        playlistVM.newTokensResponse.observe(
            viewLifecycleOwner,
            { saveTokens(it, requireContext()) })
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

    private fun descriptionIsEmpty(binding: FragmentPlaylistBinding) {
        val paramsAppBar = binding.appBarPlaylist.layoutParams
        paramsAppBar.height = convertDpToPx(340, resources)
        binding.appBarPlaylist.layoutParams = paramsAppBar

        val paramsMargin: ViewGroup.MarginLayoutParams =
            binding.llDetailsPlaylist.layoutParams as ViewGroup.MarginLayoutParams
        paramsMargin.bottomMargin = convertDpToPx(10, resources)
        binding.tvDescriptionPlaylist.visibility = View.GONE
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
        playlistVM.auth.value =
            resources.getString(R.string.esl)
        playlistVM.refreshToken.value = refreshToken
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
    }
}