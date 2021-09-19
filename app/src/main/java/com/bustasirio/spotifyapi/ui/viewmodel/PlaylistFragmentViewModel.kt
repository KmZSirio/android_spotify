package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.TracksListModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistFragmentViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    private val limit = 20
    var page = 0
    var items: TracksListModel? = null

    val tracksResponse = MutableLiveData<TracksListModel>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val tracksUrl = MutableLiveData<String>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs
    private val redirectUri: String = "bustasirio://callback"

    fun fetchPlaylistItems() = viewModelScope.launch {
        loading.postValue(true)
        apiService.getPlaylistItems(
            tracksUrl.value!!,
            authorizationWithToken.value!!,
            "$limit",
            "${page * limit}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
                    // ! ---------------------------------------------
                    page++
                    if (items == null) {
                        items = apiServiceResp.body()
                    } else {
                        val oldItems = items?.items
                        val newItems = apiServiceResp.body()?.items
                        oldItems?.addAll(newItems!!)
                    }
                    tracksResponse.postValue(items ?: apiServiceResp.body())
                }
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        authorizationBasic.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        redirectUri
                    ).let { accServiceResp ->
                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"
                            apiService.getPlaylistItems(
                                tracksUrl.value!!,
                                authWithToken,
                                "$limit",
                                "${page * limit}"
                            ).let {
                                if (it.isSuccessful) {
                                    // ! ---------------------------------------------
                                    page++
                                    if (items == null) {
                                        items = apiServiceResp.body()
                                    } else {
                                        val oldItems = items?.items
                                        val newItems = apiServiceResp.body()?.items
                                        oldItems?.addAll(newItems!!)
                                    }
                                    tracksResponse.postValue(items ?: apiServiceResp.body())
                                    newTokensResponse.postValue(accServiceResp.body())
                                } else {
                                    Log.d("tagPlaylistViewModel", "line 67")
                                    errorResponse.postValue(it.code())
                                }
                            }

                        } else {
                            Log.d("tagPlaylistViewModel", "line 73")
                            errorResponse.postValue(accServiceResp.code())
                        }
                    }

                }
                else -> {
                    Log.d("tagPlaylistViewModel", "line 80")
                    errorResponse.postValue(apiServiceResp.code())
                }
            }
        }
    }
}