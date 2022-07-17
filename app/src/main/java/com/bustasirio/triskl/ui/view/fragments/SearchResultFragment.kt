package com.bustasirio.triskl.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.annotation.NonNull
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.*
import com.bustasirio.triskl.data.model.User
import com.bustasirio.triskl.databinding.FragmentCreateBinding
import com.bustasirio.triskl.databinding.FragmentSearchResultBinding
import com.bustasirio.triskl.ui.view.adapters.*
import com.bustasirio.triskl.ui.viewmodel.CreateViewModel
import com.bustasirio.triskl.ui.viewmodel.SearchResultViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.joda.time.DateTime

@AndroidEntryPoint
class SearchResultFragment : Fragment() {

    private val searchResultVM: SearchResultViewModel by viewModels()

    private lateinit var songsAdapter: SongsResultAdapter
    private lateinit var artistsAdapter: ArtistsResultAdapter
    private lateinit var albumsAdapter: AlbumsResultAdapter
    private lateinit var playlistsAdapter: PlaylistsResultAdapter
    private lateinit var podcastsAdapter: PodcastsResultAdapter
    private lateinit var episodesAdapter: EpisodesResultAdapter

    private var firstResponse = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSearchResultBinding.bind(view)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyGreyDarker)

        getPrefs()

        setupRVs(binding)

        removeMaterialScrollEffect(binding)
        binding.etSearchResult.showKeyboard()
        binding.etSearchResult.requestFocus()
        binding.chipGroupSearchResult.check(R.id.chipSongsSearchResult)
        binding.hsvChipsSearchResult.visibility = View.GONE

        binding.etSearchResult.textChanges().debounce(1000)
            .onEach {
                val text = binding.etSearchResult.text.toString()
                if (text.isNotBlank()) {
                    searchResultVM.query.value = text
                    searchResultVM.safeFetchQuery()
                }
            }
            .launchIn(lifecycleScope)

        // * Chips related
        binding.chipGroupSearchResult.setOnCheckedChangeListener { chipGroup, _ ->

            when (chipGroup.checkedChipId) {
                binding.chipSongsSearchResult.id -> {
                    hideRVs(binding)
                    binding.rvSongsSearchResult.visibility = View.VISIBLE
                }
                binding.chipArtistsSearchResult.id -> {
                    hideRVs(binding)
                    binding.rvArtistsSearchResult.visibility = View.VISIBLE
                }
                binding.chipAlbumsSearchResult.id -> {
                    hideRVs(binding)
                    binding.rvAlbumsSearchResult.visibility = View.VISIBLE
                }
                binding.chipPlaylistsSearchResult.id -> {
                    hideRVs(binding)
                    binding.rvPlaylistsSearchResult.visibility = View.VISIBLE
                }
                binding.chipEpisodesSearchResult.id -> {
                    hideRVs(binding)
                    binding.rvEpisodesSearchResult.visibility = View.VISIBLE
                }
                binding.chipPodcastsSearchResult.id -> {
                    hideRVs(binding)
                    binding.nsvPodcastSearchResult.visibility = View.VISIBLE
                }
            }
        }

        // * SEARCH
        searchResultVM.searchResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        binding.hsvChipsSearchResult.visibility = View.VISIBLE
                        if (!firstResponse) {
                            firstResponse = true
                            binding.rvSongsSearchResult.visibility = View.VISIBLE
                        }
                        if (!response.data.tracks.tracks.isNullOrEmpty())
                            songsAdapter.differ.submitList(response.data.tracks.tracks.toList())
                        if (!response.data.artists.artists.isNullOrEmpty())
                            artistsAdapter.differ.submitList(response.data.artists.artists.toList())
                        if (!response.data.albums.albums.isNullOrEmpty())
                            albumsAdapter.differ.submitList(response.data.albums.albums.toList())
                        if (!response.data.playlists.playlists.isNullOrEmpty())
                            playlistsAdapter.differ.submitList(response.data.playlists.playlists.toList())
                        if (!response.data.shows.shows.isNullOrEmpty())
                            podcastsAdapter.differ.submitList(response.data.shows.shows.toList())
                        if (!response.data.episodes.episodes.isNullOrEmpty())
                            episodesAdapter.differ.submitList(response.data.episodes.episodes.toList())
                    }
                }
                is Resource.Error -> {
                    if (response.message == "403") {
                        showToast(requireContext(), getString(R.string.told_you), true)
                        logOut(requireActivity(), requireContext())
                    } else {
                        errorToast(response.message ?: "", requireContext())
                    }
                }
                is Resource.Loading -> {

                }
            }
        })

        searchResultVM.newTokensResponse.observe(viewLifecycleOwner, { saveTokens(it, requireContext()) })
    }

    private fun removeMaterialScrollEffect(binding: FragmentSearchResultBinding) {
        binding.hsvChipsSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvSongsSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvArtistsSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvAlbumsSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvPlaylistsSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvEpisodesSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvPodcastsSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
        binding.nsvPodcastSearchResult.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun setupRVs(binding: FragmentSearchResultBinding) {
        setupSongs(binding)
        setupArtists(binding)
        setupAlbums(binding)
        setupPlaylists(binding)
        setupPodcasts(binding)
        setupEpisodes(binding)
    }

    private fun setupSongs(binding: FragmentSearchResultBinding) {
        songsAdapter = SongsResultAdapter()
        binding.rvSongsSearchResult.apply {
            adapter = songsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun setupArtists(binding: FragmentSearchResultBinding) {
        artistsAdapter = ArtistsResultAdapter()
        binding.rvArtistsSearchResult.apply {
            adapter = artistsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun setupAlbums(binding: FragmentSearchResultBinding) {
        albumsAdapter = AlbumsResultAdapter()
        binding.rvAlbumsSearchResult.apply {
            adapter = albumsAdapter
            layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun setupPlaylists(binding: FragmentSearchResultBinding) {
        playlistsAdapter = PlaylistsResultAdapter()
        binding.rvPlaylistsSearchResult.apply {
            adapter = playlistsAdapter
            layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun setupPodcasts(binding: FragmentSearchResultBinding) {
        podcastsAdapter = PodcastsResultAdapter()
        binding.rvPodcastsSearchResult.apply {
            adapter = podcastsAdapter
//            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun setupEpisodes(binding: FragmentSearchResultBinding) {
        episodesAdapter = EpisodesResultAdapter()
        binding.rvEpisodesSearchResult.apply {
            adapter = episodesAdapter
//            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun hideRVs(binding: FragmentSearchResultBinding) {
        binding.rvSongsSearchResult.visibility = View.GONE
        binding.rvArtistsSearchResult.visibility = View.GONE
        binding.rvAlbumsSearchResult.visibility = View.GONE
        binding.rvPlaylistsSearchResult.visibility = View.GONE
        binding.nsvPodcastSearchResult.visibility = View.GONE
    }

    private fun View.showKeyboard() = ViewCompat.getWindowInsetsController(this)
        ?.show(WindowInsetsCompat.Type.ime())

    @ExperimentalCoroutinesApi
    @CheckResult
    fun EditText.textChanges(): Flow<CharSequence?> {
        return callbackFlow<CharSequence?> {
            val listener = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = Unit
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    trySend(s)
                }
            }
            addTextChangedListener(listener)
            awaitClose { removeTextChangedListener(listener) }
        }.onStart { emit(text) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
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

        searchResultVM.authorizationWithToken.value = "$tokenType $accessToken"
        searchResultVM.refreshToken.value = refreshToken
    }
}