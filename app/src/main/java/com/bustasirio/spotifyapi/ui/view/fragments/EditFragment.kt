package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.*
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.data.model.User
import com.bustasirio.spotifyapi.databinding.FragmentEditBinding
import com.bustasirio.spotifyapi.databinding.FragmentProfileBinding
import com.bustasirio.spotifyapi.ui.viewmodel.EditViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.RequestBody

@AndroidEntryPoint
class EditFragment : Fragment() {

    private val editVM: EditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val playlist =
            arguments!!.getParcelable<Playlist>(getString(R.string.arg_playlist_to_edit))!!

        val binding = FragmentEditBinding.bind(view)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        // * Setting data to components
        if (playlist.images.isNotEmpty()) Picasso.get().load(playlist.images[0].url)
            .into(binding.ivPlaylistEdit)
        else binding.ivPlaylistEdit.setImageResource(R.drawable.playlist_cover)

        binding.edTxNameEdit.setText(playlist.name)

        if (!playlist.description.isNullOrEmpty()) {
            binding.edTxDescriptionEdit.visibility = View.VISIBLE
            binding.edTxDescriptionEdit.setText(playlist.description)
        }
        else binding.btnAddDescriptionEdit.visibility = View.VISIBLE

        binding.btnSaveEdit.setOnClickListener {
            var editedName = binding.edTxNameEdit.text.toString()
            val editedDesc = binding.edTxDescriptionEdit.text.toString()

            if (editedName == playlist.name && editedDesc == playlist.description) {
                Toast.makeText(requireContext(), "No detail has changed", Toast.LENGTH_SHORT).show()
            } else {
                if (editedName == "") editedName = playlist.name

                var json = if (editedDesc == "") "{\"name\":\"$editedName\"}"
                else "{\"name\":\"$editedName\",\"description\":\"$editedDesc\"}"
                val body = RequestBody.create(MediaType.parse("text/plain"), json)

                getPrefs()
                editVM.requestBody.value = body
                editVM.editPlaylistDetails(playlist.id)
            }
        }

        binding.btnAddDescriptionEdit.setOnClickListener {
            binding.btnAddDescriptionEdit.visibility = View.GONE
            binding.edTxDescriptionEdit.visibility = View.VISIBLE
        }

        binding.ibCloseEdit.setOnClickListener {
            removeAnnoyingFrag(requireActivity().supportFragmentManager)
//            removeAnnoyingFrags(requireActivity().supportFragmentManager)
        }

        editVM.changedResponse.observe(viewLifecycleOwner, {
            if (it) {
                // * Hide the keyboard
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)

                val bundle = Bundle()
                bundle.putString(getString(R.string.create_fragment), "reload")
                requireActivity().supportFragmentManager.setFragmentResult(
                    getString(R.string.create_fragment),
                    bundle
                )
                removeAnnoyingFrags(requireActivity().supportFragmentManager)

                Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
            }
        })

        editVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        editVM.newTokensResponse.observe(viewLifecycleOwner, { saveTokens(it, requireContext()) })
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

        editVM.authorizationWithToken.value = "$tokenType $accessToken"
        editVM.auth.value =
            resources.getString(R.string.esl)
        editVM.refreshToken.value = refreshToken
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor =
            requireActivity().getColor(R.color.spotifyBlueGrey)
    }
}