package com.bustasirio.spotifyapi.ui.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        removeAnnoyingFrag(requireActivity())
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack);

        Log.d("tagSearchFragment", "${requireActivity().supportFragmentManager.fragments}")

    }
}