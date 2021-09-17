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

    val tracksResponse = MutableLiveData<TracksListModel>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

//    val tracksUrl = MutableLiveData<String>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs
    private val redirectUri: String = "bustasirio://callback"

    fun fetchPlaylistItems(url: String) = viewModelScope.launch {
        apiService.getPlaylistItems(
            url,
            authorizationWithToken.value!!,
            "20"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
//                    Log.d("tagHomeViewModel", "Line 40. ${apiServiceResp.body()}")
                    Log.d("tagPlaylistFragmentViewModel", "Href: ${apiServiceResp.body()!!.href}")
                    Log.d("tagPlaylistFragmentViewModel", "Name: ${apiServiceResp.body()!!.items[0].track.name}")
                    Log.d("tagPlaylistFragmentViewModel", "Artist: ${apiServiceResp.body()!!.items[0].track.artists[0].name}")
                    tracksResponse.postValue(apiServiceResp.body())
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
                                url,
                                authWithToken,
                                "20"
                            ).let {
                                if (it.isSuccessful) {
//                                    Log.d("tagHomeViewModel", "Line 60. ${it.body()!!.artists[0].name}")
                                    Log.d("tagPlaylistFragmentViewModel", "Name it: ${it.body()!!.items[0].track.name}")
                                    Log.d("tagPlaylistFragmentViewModel", "Artist it: ${it.body()!!.items[0].track.artists[0].name}")
                                    tracksResponse.postValue(it.body())
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