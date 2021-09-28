package com.bustasirio.spotifyapi.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.core.*
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.spotifyapi.data.model.Category
import com.bustasirio.spotifyapi.databinding.FragmentSearchBinding
import com.bustasirio.spotifyapi.ui.view.adapters.CategoryAdapter
import com.bustasirio.spotifyapi.ui.view.adapters.FeaturedAdapter
import com.bustasirio.spotifyapi.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val searchVM: SearchViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var featuredAdapter: FeaturedAdapter

    var isLoading = false
    var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        removeAnnoyingFrags(requireActivity().supportFragmentManager)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSearchBinding.bind(view)
        binding.rvCategoriesSearch.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvFeaturedSearch.overScrollMode = View.OVER_SCROLL_NEVER
        binding.nestedScrollSearch.overScrollMode = View.OVER_SCROLL_NEVER

        val fmt = DateTime().toLocalDateTime()
        searchVM.timestamp.value = fmt.toString().dropLast(4)

        getPrefs()
        searchVM.fetchFeaturedPlaylists()
        searchVM.fetchCategories()
        setupCategoryRW(binding)
        setupFeaturedRW(binding)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        // * CATEGORIES
        searchVM.categoriesResponse.observe(viewLifecycleOwner, {
            hideProgressBar(view)

            categoryAdapter.differ.submitList(it.categories.items.toList())
            val totalPages = it.categories.total / QUERY_GRID_SIZE + 1
            isLastPage = searchVM.page == totalPages

            if (!isLastPage) searchVM.fetchCategories()
        })
        categoryAdapter.setOnItemClickListener { fragTransCategory(it) }

        // * FEATURED PLAYLISTS
        searchVM.featuredResponse.observe(viewLifecycleOwner, {
            binding.tvFeaturedSearch.text = it.message

            featuredAdapter.differ.submitList(it.playlists.items.toList())
        })
        featuredAdapter.setOnItemClickListener {
            fragTransPlaylist(
                requireActivity(),
                getString(R.string.tracks_url),
                it,
                getString(R.string.owner_boolean),
                false
            )
        }

        searchVM.loading.observe(viewLifecycleOwner, { if (it) showProgressBar(view) })

        searchVM.errorResponse.observe(viewLifecycleOwner, { errorToast(it, requireContext()) })

        searchVM.newTokensResponse.observe(viewLifecycleOwner, { saveTokens(it, requireContext()) })
    }

    private fun setupCategoryRW(binding: FragmentSearchBinding) {
        categoryAdapter = CategoryAdapter()
        binding.rvCategoriesSearch.apply {
            adapter = categoryAdapter
            layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun setupFeaturedRW(binding: FragmentSearchBinding) {
        featuredAdapter = FeaturedAdapter()
        binding.rvFeaturedSearch.apply {
            adapter = featuredAdapter
            layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
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

        val country =
            sharedPrefs.getString(getString(R.string.spotify_country), "") ?: ""
        val language =
            sharedPrefs.getString(getString(R.string.spotify_language), "en_US") ?: "en_US"

//        Log.d("tagSearchFragment", country)

        searchVM.language.value = language
        searchVM.country.value = country
        searchVM.authorizationWithToken.value = "$tokenType $accessToken"
        searchVM.auth.value =
            resources.getString(R.string.esl)
        searchVM.refreshToken.value = refreshToken
    }
}