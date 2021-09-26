package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.spotifyapi.core.fragTransPlaylist
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.core.saveTokens
import com.bustasirio.spotifyapi.data.model.Category
import com.bustasirio.spotifyapi.databinding.FragmentCategoryBinding
import com.bustasirio.spotifyapi.ui.view.adapters.CategoryPlaylistAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.CategoryViewModel
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private var title = ""

    private val categoryVM: CategoryViewModel by viewModels()

    private var categoryPlaylistAdapter = CategoryPlaylistAdapter()

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        val categoryPlaylists =
            arguments?.getParcelable<Category>(getString(R.string.category_playlists))

        val binding = FragmentCategoryBinding.bind(view)
        binding.rvCategory.overScrollMode = View.OVER_SCROLL_NEVER

        requireActivity().window.statusBarColor =
            requireActivity().getColor(R.color.spotifyBlueGrey)

        getPrefs()
        if (categoryPlaylists != null) {
            title = categoryPlaylists.name
            binding.tvTitleCategory.text = title
            categoryVM.categoryId.value = categoryPlaylists.href.drop(45)
            categoryVM.fetchCategoryPlaylists()
            setupRW(binding)
        }

        binding.ibBackCategory.setOnClickListener { removeAnnoyingFrag(requireActivity().supportFragmentManager) }

        // * To hide title from the collapsing bar when extended
        var isShow = true
        var scrollRange = -1
        binding.appBarCategory.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                binding.collapsingCategory.title = title
                isShow = true
                binding.toolbarCategory.background =
                    resources.getDrawable(R.drawable.gradient_collapsed, resources.newTheme())
            } else if (isShow) {
                binding.collapsingCategory.title =
                    " " //careful there should a space between double quote otherwise it wont work
                isShow = false
                binding.toolbarCategory.background = null
            }
        })

        // * CATEGORY'S PLAYLISTS
        categoryVM.playlistsResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)

            categoryPlaylistAdapter.differ.submitList(it.playlists.items.toList())
            val totalPages = it.playlists.total / QUERY_GRID_SIZE + 2
            isLastPage = categoryVM.page == totalPages
        })
        categoryPlaylistAdapter.setOnItemClickListener {
            fragTransPlaylist(
                requireActivity(),
                getString(R.string.tracks_url),
                it
            )
        }

        categoryVM.loading.observe(viewLifecycleOwner, { if (it) showProgressBar(view) })

        categoryVM.errorResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)
            when {
                it == 404 -> {
                    Toast.makeText(
                        context,
                        "Seems like this is not available, try again later.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                it != null -> {
                    Toast.makeText(context, "Error: $it, try again later.", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    Toast.makeText(context, "Error. Try again later.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })

        categoryVM.newTokensResponse.observe(
            viewLifecycleOwner,
            { saveTokens(it, requireContext()) })
    }

    private fun setupRW(binding: FragmentCategoryBinding) {
        categoryPlaylistAdapter = CategoryPlaylistAdapter()
        binding.rvCategory.apply {
            adapter = categoryPlaylistAdapter
            layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
            addOnScrollListener(this@CategoryFragment.scrollListener)
        }
    }

    private fun hideProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressCategory)
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressCategory)
        progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                categoryVM.fetchCategoryPlaylists()
                isScrolling = false
            }
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

        categoryVM.authorizationWithToken.value = "$tokenType $accessToken"
        categoryVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        categoryVM.refreshToken.value = refreshToken
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)
    }
}