package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.convertDpToPx
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.databinding.FragmentPlaylistBinding
import com.bustasirio.spotifyapi.ui.view.adapters.PlaylistAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.PlaylistFragmentViewModel
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private val playlistFragmentVM: PlaylistFragmentViewModel by viewModels()
//    var tracksUrl: String? = null

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

        val binding = FragmentPlaylistBinding.bind(view)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlueGrey);

        getPrefs()
        playlistFragmentVM.fetchPlaylistItems(playlist!!.tracks.href)

        binding.collapsingPlaylist.contentScrim = resources.getDrawable(R.drawable.gradient_collapsed, resources.newTheme())
        binding.rvPlaylist.overScrollMode = View.OVER_SCROLL_NEVER

        binding.tvTitlePlaylist.text = playlist!!.name
        binding.tvDescriptionPlaylist.text = playlist!!.description
        binding.tvOwnerPlaylist.text = playlist!!.owner.display_name

        // * Appbar height depending if the playlist has a description
        if (playlist!!.description.isNullOrEmpty()){
            val paramsAppBar = binding.appBarPlaylist.layoutParams
            paramsAppBar.height = convertDpToPx(330, resources)
            binding.appBarPlaylist.layoutParams = paramsAppBar

            val paramsMargin : ViewGroup.MarginLayoutParams = binding.llDetailsPlaylist.layoutParams as ViewGroup.MarginLayoutParams
            paramsMargin.bottomMargin = convertDpToPx(10, resources)
            binding.tvDescriptionPlaylist.visibility = View.GONE
        }

        if (playlist.images.isNotEmpty()) {
            Picasso.get().load(playlist.images[0].url).into(binding.ivPlaylist)
        } else {
            binding.ivPlaylist.setImageResource(R.drawable.no_artist)
        }

        binding.ibBackPlaylist.setOnClickListener {
            removeAnnoyingFrag(requireActivity())
        }

        // * To hide title from the collapsing bar when extended
        var isShow = true
        var scrollRange = -1
        binding.appBarPlaylist.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1){
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0){
                binding.collapsingPlaylist.title = playlist!!.name
                isShow = true
            } else if (isShow){
                binding.collapsingPlaylist.title = " " //careful there should a space between double quote otherwise it wont work
                isShow = false
            }
        })

        // * TracksResponse
        playlistFragmentVM.tracksResponse.observe(viewLifecycleOwner, {

            Log.d("tagPlaylistFragment","${it.items[0].track.name}")
            Log.d("tagPlaylistFragment","${it.items[0].track.artists}")
//            title = it.
            val adapter = PlaylistAdapter(it.items)
            binding.rvPlaylist.adapter = adapter

            adapter.previewUrl.observe(viewLifecycleOwner, { url -> Log.d("tagPlaylistFragment", "Url: $url")})
        })

        playlistFragmentVM.errorResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT)
                    .show()
            }
        })


        // ! FIXME Send <<it>> to a fun on helpers
        playlistFragmentVM.newTokensResponse.observe(viewLifecycleOwner, {
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

        playlistFragmentVM.authorizationWithToken.value = "$tokenType $accessToken"
        playlistFragmentVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        playlistFragmentVM.refreshToken.value = refreshToken
    }
}