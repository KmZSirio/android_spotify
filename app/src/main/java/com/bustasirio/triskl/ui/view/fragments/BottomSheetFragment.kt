package com.bustasirio.triskl.ui.view.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bustasirio.triskl.R
import com.bustasirio.triskl.data.model.Track
import com.bustasirio.triskl.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import com.bustasirio.triskl.core.*
import com.bustasirio.triskl.ui.viewmodel.BottomSheetViewModel
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

        val spotifyInstalled = isItInstalled(getString(R.string.spotify_package), requireContext().packageManager)
        if (!spotifyInstalled) binding.spotifyBottomSheet.text = getString(R.string.get_spotify_free)

        val bottomSheet: FrameLayout =
            dialog!!.findViewById(com.google.android.material.R.id.design_bottom_sheet)

        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

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
            bottomSheetVM.safeAddToQueue(track.uri)
        }

        binding.shareBottomSheet.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, track.external_urls.spotify)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share to:")
            startActivity(shareIntent)
            removeAnnoyingFrag(requireActivity().supportFragmentManager)
        }

        binding.spotifyBottomSheet.setOnClickListener {
            if (spotifyInstalled) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(track.uri)
                intent.putExtra(
                    Intent.EXTRA_REFERRER,
                    Uri.parse("android-app://" + requireContext().packageName)
                )
                startActivity(intent)
                removeAnnoyingFrag(requireActivity().supportFragmentManager)
            } else {
                val spotifyPackage = getString(R.string.spotify_package)
                val referrer =
                    "adjust_campaign=PACKAGE_NAME&adjust_tracker=ndjczk&utm_source=adjust_preinstall"

                try {
                    val uri = Uri.parse("market://details")
                        .buildUpon()
                        .appendQueryParameter("id", spotifyPackage)
                        .appendQueryParameter("referrer", referrer)
                        .build()
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (ignored: ActivityNotFoundException) {
                    val uri = Uri.parse("https://play.google.com/store/apps/details")
                        .buildUpon()
                        .appendQueryParameter("id", spotifyPackage)
                        .appendQueryParameter("referrer", referrer)
                        .build()
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                removeAnnoyingFrag(requireActivity().supportFragmentManager)
            }
        }

        bottomSheetVM.completedResponse.observe(viewLifecycleOwner, { response ->
            when(response) {
                is Resource.Success -> {
                    when(response.data) {
                        204 -> {
                            showToast(requireContext(), getString(R.string.added_to_queue))
                            removeAnnoyingFrag(requireActivity().supportFragmentManager)
                        }
                        403 -> {
                            showToast(requireContext(), getString(R.string.queue_403))
                            showToast(requireContext(), getString(R.string.get_premium), true)
                        }
                        404 -> showToast(requireContext(), getString(R.string.device_no_found))
                    }
                }
                is Resource.Error -> { errorToast(response.message ?: "", requireContext()) }
                is Resource.Loading -> {}
            }
        })

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