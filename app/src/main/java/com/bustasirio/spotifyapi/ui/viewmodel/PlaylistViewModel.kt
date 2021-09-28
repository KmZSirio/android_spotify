package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants.Companion.QUERY_SIZE
import com.bustasirio.spotifyapi.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.SavedTracksModel
import com.bustasirio.spotifyapi.data.model.TracksListModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    var page = 0
    private var items: TracksListModel? = null

    val tracksResponse = MutableLiveData<TracksListModel>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val tracksUrl = MutableLiveData<String>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun fetchPlaylistItems() = viewModelScope.launch {
        loading.postValue(true)

        apiService.getPlaylistItems(
            tracksUrl.value ?: "",
            authorizationWithToken.value!!,
            "$QUERY_SIZE",
            "${page * QUERY_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> tracksSuccessful(apiServiceResp)
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        auth.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        REDIRECT_URI
                    ).let { accServiceResp ->

                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.getPlaylistItems(
                                tracksUrl.value!!,
                                authWithToken,
                                "$QUERY_SIZE",
                                "${page * QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    tracksSuccessful(it)
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

    private fun tracksSuccessful(response: Response<TracksListModel>) {
        page++
        if (items == null) {
            items = response.body()
        } else {
            val oldItems = items?.items
            val newItems = response.body()?.items
            oldItems?.addAll(newItems!!)
        }
        tracksResponse.postValue(items ?: response.body())
    }
}