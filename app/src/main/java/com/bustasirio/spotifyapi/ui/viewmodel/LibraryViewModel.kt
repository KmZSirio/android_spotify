package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.data.model.PlaylistsModel
import com.bustasirio.spotifyapi.data.model.User
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    private val limit = 20
    var page = 0
    var playlists: PlaylistsModel? = null

    val playlistsResponse = MutableLiveData<PlaylistsModel>()
    val userResponse = MutableLiveData<User>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun fetchCurrentUserProfile() = viewModelScope.launch {
        apiService.getCurrentUserProfile(
            authorizationWithToken.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> userResponse.postValue(apiServiceResp.body())
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

                            apiService.getCurrentUserProfile(
                                authWithToken
                            ).let {
                                if (it.isSuccessful) {
                                    userResponse.postValue(it.body())
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

    fun fetchCurrentUserPlaylists() = viewModelScope.launch {
        loading.postValue(true)
//        Log.d("tagLibraryFragmentVM", "$number")
        apiService.getCurrentUserPlaylists(
            authorizationWithToken.value!!,
            "$limit",
            "${page * limit}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> { playlistsSuccessful(apiServiceResp) }
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
                            apiService.getCurrentUserPlaylists(
                                authWithToken,
                                "$limit",
                                "${page * limit}"
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

    private fun playlistsSuccessful(response: Response<PlaylistsModel>) {
        page++
        if (playlists == null) {
            playlists = response.body()
        } else {
            val oldPlaylists = playlists?.playlists
            val newPlaylists = response.body()?.playlists
            oldPlaylists?.addAll(newPlaylists!!)
        }
        playlistsResponse.postValue(playlists ?: response.body())
    }
}