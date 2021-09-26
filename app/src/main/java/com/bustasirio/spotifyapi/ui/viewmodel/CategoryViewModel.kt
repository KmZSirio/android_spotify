package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.CategoryModel
import com.bustasirio.spotifyapi.data.model.CategoryPlaylistsModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    var page = 0
    private var playlists: CategoryPlaylistsModel? = null

    val playlistsResponse = MutableLiveData<CategoryPlaylistsModel>()

    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    val categoryId = MutableLiveData<String>()

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    val country = MutableLiveData<String>()

    // * CATEGORY'S PLAYLISTS
    fun fetchCategoryPlaylists() = viewModelScope.launch {
        loading.postValue(true)

        apiService.getCategoryPlaylists(
            authorizationWithToken.value!!,
            categoryId.value ?: "",
            if (country.value!!.isEmpty()) null else country.value!!,
            "$QUERY_GRID_SIZE",
            "${page * QUERY_GRID_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> playlistsSuccessful(apiServiceResp)
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        authorizationBasic.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        Constants.REDIRECT_URI
                    ).let { accServiceResp ->

                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.getCategoryPlaylists(
                                authWithToken,
                                categoryId.value ?: "",
                                if (country.value!!.isEmpty()) null else country.value!!,
                                "$QUERY_GRID_SIZE",
                                "${page * QUERY_GRID_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    playlistsSuccessful(it)
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

    private fun playlistsSuccessful(response: Response<CategoryPlaylistsModel>) {
        page++
        if (playlists == null) {
            playlists = response.body()
        } else {
            val oldItems = playlists?.playlists?.items
            val newItems = response.body()?.playlists?.items
            oldItems?.addAll(newItems!!)
        }
        playlistsResponse.postValue(playlists ?: response.body())
    }
}