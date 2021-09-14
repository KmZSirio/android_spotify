package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.TopArtistsModel
import com.bustasirio.spotifyapi.data.model.TopTracksModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    val topArtistsResponse = MutableLiveData<TopArtistsModel>()
    val errorResponse = MutableLiveData<Int>()

    val topTracksMonthResponse = MutableLiveData<TopTracksModel>()
    val topTracksSixMonthsResponse = MutableLiveData<TopTracksModel>()
    val topTracksLifetimeResponse = MutableLiveData<TopTracksModel>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs
    private val redirectUri: String = "bustasirio://callback"

    // ! FIXME Eliminate repeatable code
    fun fetchTopArtists() = viewModelScope.launch {
        apiService.getTopArtists(
            authorizationWithToken.value!!,
            "10"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
//                    Log.d("tagHomeViewModel", "Line 40. ${apiServiceResp.body()}")
                    topArtistsResponse.postValue(apiServiceResp.body())
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
                            apiService.getTopArtists(
                                authWithToken,
                                "10"
                            ).let {
                                if (it.isSuccessful) {
//                                    Log.d("tagHomeViewModel", "Line 60. ${it.body()!!.artists[0].name}")
                                    topArtistsResponse.postValue(it.body())
                                    newTokensResponse.postValue(accServiceResp.body())
                                } else {
                                    Log.d("tagHomeViewModel", "line 67")
                                    errorResponse.postValue(it.code())
                                }
                            }

                        } else {
                            Log.d("tagHomeViewModel", "line 73")
                            errorResponse.postValue(accServiceResp.code())
                        }
                    }

                }
                else -> {
                    Log.d("tagHomeViewModel", "line 80")
                    errorResponse.postValue(apiServiceResp.code())
                }
            }
        }
    }

    fun fetchTopTracks(term : String) = viewModelScope.launch {
        apiService.getTopTracks(
            authorizationWithToken.value!!,
            term,
            "10"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
//                    Log.d("tagHomeViewModel", "Line 40. ${apiServiceResp.body()}")
                    if (term == "short_term") topTracksMonthResponse.postValue(apiServiceResp.body())
                    if (term == "medium_term") topTracksSixMonthsResponse.postValue(apiServiceResp.body())
                    if (term == "long_term") topTracksLifetimeResponse.postValue(apiServiceResp.body())
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
                            apiService.getTopTracks(
                                authWithToken,
                                term,
                                "10"
                            ).let {
                                if (it.isSuccessful) {
//                                    Log.d("tagHomeViewModel", "Line 60. ${it.body()!!.artists[0].name}")
                                    if (term == "short_term") topTracksMonthResponse.postValue(it.body())
                                    if (term == "medium_term") topTracksSixMonthsResponse.postValue(it.body())
                                    if (term == "long_term") topTracksLifetimeResponse.postValue(it.body())
                                    newTokensResponse.postValue(accServiceResp.body())
                                } else {
                                    Log.d("tagHomeViewModel", "line 117")
                                    errorResponse.postValue(it.code())
                                }
                            }

                        } else {
                            Log.d("tagHomeViewModel", "line 123")
                            errorResponse.postValue(accServiceResp.code())
                        }
                    }

                }
                else -> {
                    Log.d("tagHomeViewModel", "line 130")
                    errorResponse.postValue(apiServiceResp.code())
                }
            }
        }
    }
}