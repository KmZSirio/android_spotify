package com.bustasirio.spotifyapi.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.spotifyapi.data.model.*
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    var page = 0
    private var categories: CategoryModel? = null

    val categoriesResponse = MutableLiveData<CategoryModel>()
    val featuredResponse = MutableLiveData<FeaturedPlaylistsModel>()

    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    val country = MutableLiveData<String>()
    val language = MutableLiveData<String>()
    val timestamp = MutableLiveData<String>()

    // * CATEGORIES
    fun fetchCategories() = viewModelScope.launch {
        loading.postValue(true)

        apiService.getCategories(
            authorizationWithToken.value!!,
            if (country.value!!.isEmpty()) null else country.value!!,
            language.value!!,
            "$QUERY_GRID_SIZE",
            "${page * QUERY_GRID_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> categoriesSuccessful(apiServiceResp)
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        auth.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        Constants.REDIRECT_URI
                    ).let { accServiceResp ->

                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.getCategories(
                                authWithToken,
                                if (country.value!!.isEmpty()) null else country.value!!,
                                language.value!!,
                                "$QUERY_GRID_SIZE",
                                "${page * QUERY_GRID_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    categoriesSuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else errorResponse.postValue(it.code())
                            }
                        }
                        else errorResponse.postValue(accServiceResp.code())
                    }
                }
                else -> errorResponse.postValue(apiServiceResp.code())
            }
        }
    }

    // * FEATURED PLAYLISTS
    fun fetchFeaturedPlaylists() = viewModelScope.launch {

        apiService.getFeaturedPlaylists(
            authorizationWithToken.value!!,
            if (country.value!!.isEmpty()) null else country.value!!,
            language.value!!,
            timestamp.value!!,
            "$QUERY_GRID_SIZE",
            "${page * QUERY_GRID_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> featuredResponse.postValue(apiServiceResp.body())
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        auth.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        Constants.REDIRECT_URI
                    ).let { accServiceResp ->

                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.getFeaturedPlaylists(
                                authWithToken,
                                if (country.value!!.isEmpty()) null else country.value!!,
                                language.value!!,
                                timestamp.value!!,
                                "$QUERY_GRID_SIZE",
                                "${page * QUERY_GRID_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    featuredResponse.postValue(it.body())
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else errorResponse.postValue(it.code())
                            }
                        }
                        else errorResponse.postValue(accServiceResp.code())
                    }
                }
                else -> errorResponse.postValue(apiServiceResp.code())
            }
        }
    }

    private fun categoriesSuccessful(response: Response<CategoryModel>) {
        page++
        if (categories == null) {
            categories = response.body()
        } else {
            val oldItems = categories?.categories?.items
            val newItems = response.body()?.categories?.items
            oldItems?.addAll(newItems!!)
        }
        categoriesResponse.postValue(categories ?: response.body())
    }
}