package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.core.replaceFrag
import com.bustasirio.spotifyapi.core.saveTokens
import com.bustasirio.spotifyapi.data.model.User
import com.bustasirio.spotifyapi.databinding.FragmentCreateBinding
import com.bustasirio.spotifyapi.ui.viewmodel.CreateViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.RequestBody


@AndroidEntryPoint
class CreateFragment : Fragment() {

    private val createVM: CreateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        removeAnnoyingFrag(requireActivity())
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val user = arguments!!.getParcelable<User>(getString(R.string.user_object))
        createVM.endpointUrl.value = "https://api.spotify.com/v1/users/${user!!.id}/playlists"

        val binding = FragmentCreateBinding.bind(view)

        requireActivity().window.statusBarColor =
            requireActivity().getColor(R.color.spotifyBlueGrey)
        binding.cgTimeCreate.check(R.id.cFirstTimeCreate)
        binding.cgNumberCreate.check(R.id.cFirstNumberCreate)
        var timeChecked = 1
        var numberChecked = 1

        binding.toolbarCreate.setNavigationOnClickListener { removeAnnoyingFrag(requireActivity().supportFragmentManager) }

        binding.cgTimeCreate.setOnCheckedChangeListener { chipGroup, _ ->
            when (chipGroup.checkedChipId) {
                binding.cFirstTimeCreate.id -> timeChecked = 1
                binding.cSecondTimeCreate.id -> timeChecked = 2
                binding.cThirdTimeCreate.id -> timeChecked = 3
            }
        }

        binding.cgNumberCreate.setOnCheckedChangeListener { chipGroup, _ ->
            when (chipGroup.checkedChipId) {
                binding.cFirstNumberCreate.id -> numberChecked = 1
                binding.cSecondNumberCreate.id -> numberChecked = 2
                binding.cThirdNumberCreate.id -> numberChecked = 3
            }
        }

        binding.buttonCreate.setOnClickListener {
            val timeRange = when (timeChecked) {
                1 -> "last month"
                2 -> "last six months"
                3 -> "account's lifetime"
                else -> "error"
            }
            val size = when (numberChecked) {
                1 -> "10"
                2 -> "20"
                3 -> "50"
                else -> "error"
            }

            val range = when (timeChecked) {
                1 -> "short_term"
                2 -> "medium_term"
                3 -> "long_term"
                else -> "error"
            }

            val title = "Top $size songs in the $timeRange"
            val json = "{\"name\":\"$title\"}"

            val body = RequestBody.create(MediaType.parse("text/plain"), json)
            createVM.requestBody.value = body
            getPrefs()
            createVM.createPlaylist(range, size)
        }

        // * PlaylistsResponse
        createVM.playlistResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)
//
            val bundle = Bundle()
            bundle.putString(getString(R.string.createfragment), "reload")
            requireActivity().supportFragmentManager.setFragmentResult(
                getString(R.string.createfragment),
                bundle
            )
            removeAnnoyingFrag(requireActivity().supportFragmentManager)
//            replaceFrag(requireActivity(), LibraryFragment())

            Toast.makeText(requireContext(), "Playlist Created", Toast.LENGTH_SHORT)
                .show()
        })

        createVM.loading.observe(viewLifecycleOwner, {
            if (it) showProgressBar(view)
        })

        createVM.errorResponse.observe(viewLifecycleOwner, {
            if (it != null) {
                Toast.makeText(requireContext(), "Error: $it, try again later.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error. Try again later.", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        createVM.newTokensResponse.observe(viewLifecycleOwner, { saveTokens(it, requireContext()) })
    }

    private fun hideProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressCreate)
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressCreate)
        progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private var isLoading = false

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

        createVM.authorizationWithToken.value = "$tokenType $accessToken"
        createVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        createVM.refreshToken.value = refreshToken
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
    }
}