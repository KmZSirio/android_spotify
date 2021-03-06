package com.bustasirio.triskl.ui.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.*
import com.bustasirio.triskl.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.triskl.data.model.Category
import com.bustasirio.triskl.databinding.FragmentSearchBinding
import com.bustasirio.triskl.ui.view.adapters.CategoryAdapter
import com.bustasirio.triskl.ui.view.adapters.FeaturedAdapter
import com.bustasirio.triskl.ui.viewmodel.SearchViewModel
import com.google.android.material.appbar.AppBarLayout
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
        searchVM.safeFetchFeaturedPlaylists()
        searchVM.safeFetchCategories()
        setupCategoryRW(binding)
        setupFeaturedRW(binding)

        requireActivity().window.statusBarColor = requireActivity().getColor(R.color.spotifyBlack)

        // * To show the proper SearchBox depending on the extended/collapsed appBar
        var isShow = true
        var scrollRange = -1
        binding.appBarSearch.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                binding.cvExtendedSearch.visibility = View.GONE
                binding.cvCollapsedSearch.visibility = View.VISIBLE
                isShow = true
//                binding.toolbarSearch.background =
//                    resources.getDrawable(R.drawable.gradient_collapsed, resources.newTheme())
            } else if (isShow) {
                binding.cvExtendedSearch.visibility = View.VISIBLE
                binding.cvCollapsedSearch.visibility = View.GONE
            }
        })

        // * CATEGORIES
        searchVM.categoriesResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar(view)

                    categoryAdapter.differ.submitList(response.data!!.categories.items.toList())
                    val totalPages = response.data.categories.total / QUERY_GRID_SIZE + 1
                    isLastPage = searchVM.page == totalPages

                    if (!isLastPage) searchVM.safeFetchCategories()
                }
                is Resource.Error -> {
                    hideProgressBar(view)
                    if (response.message != "403")
                        errorToast(response.message ?: "", requireContext())
                }
                is Resource.Loading -> { showProgressBar(view) }
            }
        })
        categoryAdapter.setOnItemClickListener { fragTransCategory(it) }

        // * FEATURED PLAYLISTS
        searchVM.featuredResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    binding.tvFeaturedSearch.text = response.data!!.message

                    featuredAdapter.differ.submitList(response.data.playlists.items.toList())
                }
                is Resource.Error -> {
                    if (response.message == "403") {
                        showToast(requireContext(), getString(R.string.told_you), true)
                        logOut(requireActivity(), requireContext())
                    } else {
                        errorToast(response.message ?: "", requireContext())
                    }
                }
                is Resource.Loading -> { showProgressBar(view) }
            }
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

        searchVM.newTokensResponse.observe(viewLifecycleOwner, { saveTokens(it, requireContext()) })

        // * Go to SearchResult
        binding.cvCollapsedSearch.setOnClickListener {
            replaceFrag(
                requireActivity(),
                SearchResultFragment()
            )
        }
        binding.cvExtendedSearch.setOnClickListener {
            replaceFrag(
                requireActivity(),
                SearchResultFragment()
            )
        }
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
        searchVM.refreshToken.value = refreshToken
    }
}