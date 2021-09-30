package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Track
import com.bustasirio.spotifyapi.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.core.*
import com.bustasirio.spotifyapi.ui.viewmodel.BottomSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetFragment : BottomSheetDialogFragment() {

    private val bottomSheetVM: BottomSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val track = arguments!!.getParcelable<Track>(getString(R.string.arg_bottom_sheet_track))!!

        val binding = FragmentBottomSheetBinding.bind(view)
//        val size = screenSize(requireActivity())

        val bottomSheet: FrameLayout =
            dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)

        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        behavior.peekHeight = (size.heightPixels * 0.7).toInt()

        if (track.album.images.isNotEmpty()) Picasso.get().load(track.album.images[0].url)
            .into(binding.ivBottomSheet)
        else binding.ivBottomSheet.setImageResource(R.drawable.playlist_cover)

        binding.tvTrackBottomSheet.text = track.name
        binding.tvArtistBottomSheet.text = track.artists[0].name

        binding.playBottomSheet.setOnClickListener {
            reproduce(
                requireContext(),
                getString(R.string.reproduce_toast),
                track.preview_url
            )
        }

        binding.queueBottomSheet.setOnClickListener {
            getPrefs()
            bottomSheetVM.addToQueue(track.uri)
        }

        bottomSheetVM.completedResponse.observe(viewLifecycleOwner, {
            when(it) {
                204 -> showToast(requireContext(), getString(R.string.added_to_queue))
                403 -> {
                    showToast(requireContext(), getString(R.string.queue_403))
                    showToast(requireContext(), getString(R.string.get_premium), true)
                }
                404 -> showToast(requireContext(), getString(R.string.device_no_found))
            }
        })

        bottomSheetVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        bottomSheetVM.newTokensResponse.observe(viewLifecycleOwner, { saveTokens(it, requireContext()) })

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

        bottomSheetVM.authorizationWithToken.value = "$tokenType $accessToken"
        bottomSheetVM.auth.value =
            resources.getString(R.string.esl)
        bottomSheetVM.refreshToken.value = refreshToken
    }
}