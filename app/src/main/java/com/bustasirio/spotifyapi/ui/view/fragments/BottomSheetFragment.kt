package com.bustasirio.spotifyapi.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.reproduce
import com.bustasirio.spotifyapi.data.model.Track
import com.bustasirio.spotifyapi.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import android.widget.FrameLayout
import com.bustasirio.spotifyapi.core.fragTransSaved
import com.google.android.material.bottomsheet.BottomSheetBehavior


class BottomSheetFragment : BottomSheetDialogFragment() {

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
        val alreadyOnAlbum = arguments?.getBoolean(getString(R.string.album_boolean)) ?: false
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

        if (alreadyOnAlbum) binding.albumBottomSheet.visibility = View.GONE

        binding.tvTrackBottomSheet.text = track.name
        binding.tvArtistBottomSheet.text = track.artists[0].name

        binding.playBottomSheet.setOnClickListener {
            reproduce(
                requireContext(),
                getString(R.string.reproduce_toast),
                track.preview_url
            )
        }

        binding.albumBottomSheet.setOnClickListener {
            fragTransSaved(
                requireActivity(),
                getString(R.string.arg_saved_type),
                "album",
                getString(R.string.arg_album_from_bottom),
                track.album
            )
        }
    }
}