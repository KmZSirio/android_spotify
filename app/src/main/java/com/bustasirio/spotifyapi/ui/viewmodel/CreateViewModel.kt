package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.spotifyapi.core.tracksToJson
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    val playlistResponse = MutableLiveData<Playlist>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val endpointUrl = MutableLiveData<String>()
    val requestBody = MutableLiveData<RequestBody>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    private lateinit var playlist: Playlist

    fun createPlaylist(range: String, size: String) = viewModelScope.launch {
        loading.postValue(true)
        apiService.postPlaylist(
            endpointUrl.value!!,
            authorizationWithToken.value!!,
            requestBody.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.code() == 201 -> {
                    // ? -----------------------------------------------------
                    playlist = apiServiceResp.body()!!
                    fetchTopTracks(range, size, authorizationWithToken.value!! )
                }
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        authorizationBasic.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        REDIRECT_URI
                    ).let { accServiceResp ->
                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.postPlaylist(
                                endpointUrl.value!!,
                                authWithToken,
                                requestBody.value!!
                            ).let {
                                if (it.code() == 201) {
                                    // ? -----------------------------------------------------
                                    playlist = it.body()!!
                                    fetchTopTracks(range, size, authWithToken)
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

    private fun fetchTopTracks(range: String, size: String, authToken: String) = viewModelScope.launch {
        apiService.getTopTracks(
            authToken,
            range,
            size
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
                    // ? -----------------------------------------------------
                    val response = apiServiceResp.body()
                    val body = tracksToJson(response!!.tracks)
                    fillPlaylist(authToken, body)
                }
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        authorizationBasic.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        REDIRECT_URI
                    ).let { accServiceResp ->
                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.getTopTracks(
                                authWithToken,
                                range,
                                size
                            ).let {
                                if (it.isSuccessful) {
                                    // ? -----------------------------------------------------
                                    val response = it.body()
                                    val body = tracksToJson(response!!.tracks)
                                    fillPlaylist(authWithToken, body)

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

    private fun fillPlaylist(authToken: String, body: RequestBody) = viewModelScope.launch {
        apiService.postItemsToPlaylist(
            authToken,
            playlist.id,
            body
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
                    // ? -----------------------------------------------------
                    playlistResponse.postValue(playlist)
                }
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        authorizationBasic.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        REDIRECT_URI
                    ).let { accServiceResp ->
                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.postItemsToPlaylist(
                                authWithToken,
                                playlist.id,
                                body
                            ).let {
                                if (it.isSuccessful) {
                                    // ? -----------------------------------------------------
                                    playlistResponse.postValue(playlist)
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
}