package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.spotifyapi.core.errorToast
import com.bustasirio.spotifyapi.core.removeAnnoyingFrag
import com.bustasirio.spotifyapi.core.replaceFrag
import com.bustasirio.spotifyapi.data.model.Category
import com.bustasirio.spotifyapi.data.model.User
import com.bustasirio.spotifyapi.databinding.FragmentSavedBinding
import com.bustasirio.spotifyapi.databinding.FragmentSearchBinding
import com.bustasirio.spotifyapi.ui.view.adapters.CategoryAdapter
import com.bustasirio.spotifyapi.ui.view.adapters.SavedShowAdapter
import com.bustasirio.spotifyapi.ui.view.adapters.SavedSongsAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.SavedViewModel
import com.bustasirio.spotifyapi.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val searchVM: SearchViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        removeAnnoyingFrag(requireActivity().supportFragmentManager)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSearchBinding.bind(view)
        binding.rvCategoriesSearch.overScrollMode = View.OVER_SCROLL_NEVER

        getPrefs()
        searchVM.fetchCategories()
        setupCategoryRW(binding)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        // * CATEGORIES
        searchVM.categoriesResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)

            categoryAdapter.differ.submitList(it.categories.items.toList())
            val totalPages = it.categories.total / QUERY_GRID_SIZE + 1
            isLastPage = searchVM.page == totalPages
        })
        categoryAdapter.setOnItemClickListener { fragTransCategory(it) }

        searchVM.loading.observe(viewLifecycleOwner, { if (it) showProgressBar(view) })

        searchVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        searchVM.newTokensResponse.observe(viewLifecycleOwner, {
            val sharedPrefs = requireContext().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            with(sharedPrefs.edit()) {
                putString(
                    getString(R.string.spotify_access_token),
                    it.accessToken
                )
                putString(
                    getString(R.string.spotify_token_type),
                    it.tokenType
                )
                putBoolean(getString(R.string.spotify_logged), true)
                apply()
            }
        })
    }

    private fun setupCategoryRW(binding: FragmentSearchBinding) {
        categoryAdapter = CategoryAdapter()
        binding.rvCategoriesSearch.apply {
            adapter = categoryAdapter
            layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

    private fun hideProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressSearch)
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(view: View) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressSearch)
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

            val layoutManager = recyclerView.layoutManager as GridLayoutManager
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
                searchVM.fetchCategories()
                isScrolling = false
            }
        }
    }

    private fun fragTransCategory(category: Category?) {
        val fragment = CategoryFragment()

        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.category_playlists), category)
        fragment.arguments = bundle

        replaceFrag(requireActivity(), fragment)
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

        searchVM.authorizationWithToken.value = "$tokenType $accessToken"
        searchVM.authorizationBasic.value =
            resources.getString(R.string.spotify_basic)
        searchVM.refreshToken.value = refreshToken
    }
}