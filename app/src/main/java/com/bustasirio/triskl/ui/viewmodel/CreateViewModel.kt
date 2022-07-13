package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.triskl.core.ESL
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.core.tracksToJson
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.model.Playlist
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    val playlistResponse: MutableLiveData<Resource<Playlist>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val userId = MutableLiveData<String>()
    val requestBody = MutableLiveData<RequestBody>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    private lateinit var playlist: Playlist
    private lateinit var body: RequestBody

    fun safeCreatePlaylist(range: String, size: String) {
        playlistResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchTopTracks(range, size)
            else playlistResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> playlistResponse.postValue(Resource.Error("network"))
                else -> playlistResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    private fun fetchTopTracks(range: String, size: String) = viewModelScope.launch {

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
                    else playlistResponse.postValue(Resource.Error("empty"))
                }
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        ESL.ESL,
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
                                    else playlistResponse.postValue(Resource.Error("empty"))

                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else playlistResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else playlistResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> playlistResponse.postValue(Resource.Error(apiServiceResp.message()))
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
                        ESL.ESL,
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
                                else playlistResponse.postValue(Resource.Error(it.message()))

                                newTokensResponse.postValue(accServiceResp.body())
                            }
                        }
                        else playlistResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> playlistResponse.postValue(Resource.Error(apiServiceResp.message()))
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
                apiServiceResp.isSuccessful -> playlistResponse.postValue(Resource.Success(playlist))
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        ESL.ESL,
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
                                    playlistResponse.postValue(Resource.Success(playlist))
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else playlistResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else playlistResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> playlistResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }
}