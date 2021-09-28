package com.bustasirio.spotifyapi.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Track
import com.bustasirio.spotifyapi.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso

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
        val track = arguments!!.getParcelable<Track>(getString(R.string.arg_bottom_sheet_track))!!

        val binding = FragmentBottomSheetBinding.bind(view)

        if (track.album.images.isNotEmpty()) Picasso.get().load(track.album.images[0].url)
            .into(binding.ivBottomSheet)
        else binding.ivBottomSheet.setImageResource(R.drawable.playlist_cover)

        binding.tvTrackBottomSheet.text = track.name
        binding.tvArtistBottomSheet.text = track.artists[0].name
    }
}