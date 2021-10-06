package com.bustasirio.triskl.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.*
import com.bustasirio.triskl.databinding.FragmentRecentlyBinding
import com.bustasirio.triskl.ui.view.adapters.RecentlyAdapter
import com.bustasirio.triskl.ui.viewmodel.RecentlyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentlyFragment : Fragment() {

    private val recentlyVM: RecentlyViewModel by viewModels()

    private lateinit var recentlyAdapter: RecentlyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        removeAnnoyingFrag(requireActivity())
        return inflater.inflate(R.layout.fragment_recently, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRecentlyBinding.bind(view)
        setupRecyclerView(binding)

        requireActivity().window.statusBarColor =
            requireActivity().getColor(R.color.spotifyBlueGrey)


        binding.rvRecently.overScrollMode = View.OVER_SCROLL_NEVER

        getPrefs()
        recentlyVM.safeFetchRecentlyPlayed()

        binding.toolbarRecently.setNavigationOnClickListener { removeAnnoyingFrag(requireActivity().supportFragmentManager) }

        // * RecentlyResponse
        recentlyVM.recentlyResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data!!.play_histories.size == 0)
                        binding.ivEmptyRecently.visibility = View.VISIBLE
                    recentlyAdapter.differ.submitList(response.data.play_histories.toList())
                }
                is Resource.Error -> {
                    errorToast(response.message ?: "", requireContext())
                }
                is Resource.Loading -> {
                }
            }
        })

        recentlyAdapter.setOnItemClickListener {
            reproduce(
                requireContext(),
                getString(R.string.reproduce_toast),
                it.track.preview_url
            )
        }

        recentlyVM.newTokensResponse.observe(
            viewLifecycleOwner,
            { saveTokens(it, requireContext()) })
    }

    private fun setupRecyclerView(binding: FragmentRecentlyBinding) {
        recentlyAdapter = RecentlyAdapter()
        binding.rvRecently.apply {
            adapter = recentlyAdapter
            layoutManager = LinearLayoutManager(activity)
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

        recentlyVM.authorizationWithToken.value = "$tokenType $accessToken"
        recentlyVM.auth.value =
            resources.getString(R.string.esl)
        recentlyVM.refreshToken.value = refreshToken
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
    }
}