package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants
import com.bustasirio.triskl.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.triskl.core.ESL
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.*
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    var page = 0
    private var categories: CategoryModel? = null

    val categoriesResponse: MutableLiveData<Resource<CategoryModel>> = MutableLiveData()
    val featuredResponse: MutableLiveData<Resource<FeaturedPlaylistsModel>> = MutableLiveData()

    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    val country = MutableLiveData<String>()
    val language = MutableLiveData<String>()
    val timestamp = MutableLiveData<String>()

    fun safeFetchCategories() {
        categoriesResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchCategories()
            else categoriesResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when (t) {
                is IOException -> categoriesResponse.postValue(Resource.Error("network"))
                else -> categoriesResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    fun safeFetchFeaturedPlaylists() {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchFeaturedPlaylists()
            else featuredResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when (t) {
                is IOException -> featuredResponse.postValue(Resource.Error("network"))
                else -> featuredResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    // * CATEGORIES
    private fun fetchCategories() = viewModelScope.launch {

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
                        ESL.ESL,
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
                                } else categoriesResponse.postValue(Resource.Error(it.code().toString()))
                            }
                        } else categoriesResponse.postValue(Resource.Error(accServiceResp.code().toString()))
                    }
                }
                else -> categoriesResponse.postValue(Resource.Error(apiServiceResp.code().toString()))
            }
        }
    }

    // * FEATURED PLAYLISTS
    private fun fetchFeaturedPlaylists() = viewModelScope.launch {

        apiService.getFeaturedPlaylists(
            authorizationWithToken.value!!,
            if (country.value!!.isEmpty()) null else country.value!!,
            language.value!!,
            timestamp.value!!,
            "$QUERY_GRID_SIZE",
            "${page * QUERY_GRID_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> featuredResponse.postValue(
                    Resource.Success(
                        apiServiceResp.body()!!
                    )
                )
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        ESL.ESL,
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
                                    featuredResponse.postValue(Resource.Success(it.body()!!))
                                    newTokensResponse.postValue(accServiceResp.body())
                                } else featuredResponse.postValue(Resource.Error(it.code().toString()))
                            }
                        } else featuredResponse.postValue(Resource.Error(accServiceResp.code().toString()))
                    }
                } else -> featuredResponse.postValue(Resource.Error(apiServiceResp.code().toString()))
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
        categoriesResponse.postValue(Resource.Success(categories ?: response.body()!!))
    }
}