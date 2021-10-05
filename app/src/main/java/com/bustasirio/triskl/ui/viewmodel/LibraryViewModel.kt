package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.model.PlaylistsModel
import com.bustasirio.triskl.data.model.User
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    private val limit = 20
    var page = 0
    var playlists: PlaylistsModel? = null

    val playlistsResponse: MutableLiveData<Resource<PlaylistsModel>> = MutableLiveData()
    val userResponse: MutableLiveData<Resource<User>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeFetchPlaylists() {
        playlistsResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchCurrentUserPlaylists()
            else playlistsResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> playlistsResponse.postValue(Resource.Error("network"))
                else -> playlistsResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    fun safeFetchUserProfile() {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchCurrentUserProfile()
            else userResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> userResponse.postValue(Resource.Error("network"))
                else -> userResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    private fun fetchCurrentUserProfile() = viewModelScope.launch {

        apiService.getCurrentUserProfile(
            authorizationWithToken.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> userResponse.postValue(Resource.Success(apiServiceResp.body()!!))
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

                            apiService.getCurrentUserProfile(
                                authWithToken
                            ).let {
                                if (it.isSuccessful) {
                                    userResponse.postValue(Resource.Success(it.body()!!))
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else userResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else userResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> userResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }

    private fun fetchCurrentUserPlaylists() = viewModelScope.launch {

        apiService.getCurrentUserPlaylists(
            authorizationWithToken.value!!,
            "$limit",
            "${page * limit}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> { playlistsSuccessful(apiServiceResp) }
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
                            apiService.getCurrentUserPlaylists(
                                authWithToken,
                                "$limit",
                                "${page * limit}"
                            ).let {
                                if (it.isSuccessful) {
                                    playlistsSuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else playlistsResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else playlistsResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> playlistsResponse.postValue(Resource.Error(apiServiceResp.message()))
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
        playlistsResponse.postValue(Resource.Success(playlists ?: response.body()!!))
    }
}