package com.bustasirio.triskl.ui.view.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.CircleTransformation
import com.bustasirio.triskl.core.removeAnnoyingFrag
import com.bustasirio.triskl.data.model.User
import com.bustasirio.triskl.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val user = arguments!!.getParcelable<User>(getString(R.string.user_object))
        val noPlaylists = arguments.getInt(getString(R.string.no_playlists))

        val binding = FragmentProfileBinding.bind(view)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyGreen)

        if (user != null) {
            binding.tvNameProfile.text = user.displayName
            binding.tvFollowersProfile.text = "${user.followers.total}"
            binding.tvPlaylistProfile.text = "$noPlaylists"

            if (user.images.isNotEmpty()) {
                Picasso.get().load(user.images[0].url).transform(CircleTransformation())
                    .into(binding.ivProfile)
            } else {
                binding.ivProfile.setImageResource(R.drawable.user_cover)
            }
        }

        binding.ibBackProfile.setOnClickListener {
            removeAnnoyingFrag(requireActivity().supportFragmentManager)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
    }
}