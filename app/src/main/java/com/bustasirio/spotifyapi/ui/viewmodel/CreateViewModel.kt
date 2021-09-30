package com.bustasirio.spotifyapi.ui.viewmodel

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
    val emptyResponse = MutableLiveData<Boolean>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val userId = MutableLiveData<String>()
    val requestBody = MutableLiveData<RequestBody>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    private lateinit var playlist: Playlist
    private lateinit var body: RequestBody


    fun fetchTopTracks(range: String, size: String) = viewModelScope.launch {

        loading.postValue(true)
        apiService.getTopTracks(
            authorizationWithToken.value!!,
            range,
            size
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
                    val response = apiServiceResp.body()

                    if (response!!.tracks.isNotEmpty()) {
                        body = tracksToJson(response.tracks)
                        createPlaylist(authorizationWithToken.value!!)
                    }
                    else emptyResponse.postValue(true)
                }
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

                            apiService.getTopTracks(
                                authWithToken,
                                range,
                                size
                            ).let {
                                if (it.isSuccessful) {
                                    val response = it.body()

                                    if (response!!.tracks.isNotEmpty()) {
                                        body = tracksToJson(response.tracks)
                                        createPlaylist(authWithToken)
                                    }
                                    else emptyResponse.postValue(true)

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

    private fun createPlaylist(authToken: String) = viewModelScope.launch {
        apiService.postPlaylist(
            userId.value!!,
            authToken,
            requestBody.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.code() == 201 -> {
                    playlist = apiServiceResp.body()!!
                    fillPlaylist(authToken)
                }
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

                            apiService.postPlaylist(
                                userId.value!!,
                                authWithToken,
                                requestBody.value!!
                            ).let {
                                if (it.code() == 201) {
                                    playlist = it.body()!!
                                    fillPlaylist(authWithToken)
                                }
                                else errorResponse.postValue(it.code())

                                newTokensResponse.postValue(accServiceResp.body())
                            }
                        }
                        else errorResponse.postValue(accServiceResp.code())
                    }
                }
                else -> errorResponse.postValue(apiServiceResp.code())
            }
        }
    }

    private fun fillPlaylist(authToken: String) = viewModelScope.launch {
        apiService.postItemsToPlaylist(
            authToken,
            playlist.id,
            body
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> playlistResponse.postValue(playlist)
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

                            apiService.postItemsToPlaylist(
                                authWithToken,
                                playlist.id,
                                body
                            ).let {
                                if (it.isSuccessful) {
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