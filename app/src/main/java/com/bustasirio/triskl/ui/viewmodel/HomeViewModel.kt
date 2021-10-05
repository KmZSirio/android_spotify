package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.model.TopArtistsModel
import com.bustasirio.triskl.data.model.TopTracksModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    val artistsResponse: MutableLiveData<Resource<TopArtistsModel>> = MutableLiveData()
    val tracksMonthResponse: MutableLiveData<Resource<TopTracksModel>> = MutableLiveData()
    val tracksSixMonthsResponse: MutableLiveData<Resource<TopTracksModel>> = MutableLiveData()
    val tracksLifetimeResponse: MutableLiveData<Resource<TopTracksModel>> = MutableLiveData()

    val errorResponse = MutableLiveData<String>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeFetchTopArtists() {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchTopArtists()
            else errorResponse.postValue("internet")
        } catch (t: Throwable) {
            when (t) {
                is IOException -> artistsResponse.postValue(Resource.Error("network"))
                else -> artistsResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    fun safeFetchTopTracks(term : String) {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchTopTracks(term)
            else {
                if (term == "short_term") tracksMonthResponse.postValue(Resource.Error("internet"))
                if (term == "medium_term") tracksSixMonthsResponse.postValue(Resource.Error("internet"))
                if (term == "long_term") tracksLifetimeResponse.postValue(Resource.Error("internet"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> errorResponse.postValue("network")
                else -> errorResponse.postValue("conversion")
            }
        }
    }

    private fun fetchTopArtists() = viewModelScope.launch {
        apiService.getTopArtists(
            authorizationWithToken.value!!,
            "10"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> artistsResponse.postValue(Resource.Success(apiServiceResp.body()!!))
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
                            apiService.getTopArtists(
                                authWithToken,
                                "10"
                            ).let {
                                if (it.isSuccessful) {
                                    artistsResponse.postValue(Resource.Success(it.body()!!))
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else artistsResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else artistsResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> artistsResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }

    private fun fetchTopTracks(term : String) = viewModelScope.launch {
        apiService.getTopTracks(
            authorizationWithToken.value!!,
            term,
            "10"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
                    if (term == "short_term") tracksMonthResponse.postValue(Resource.Success(apiServiceResp.body()!!))
                    if (term == "medium_term") tracksSixMonthsResponse.postValue(Resource.Success(apiServiceResp.body()!!))
                    if (term == "long_term") tracksLifetimeResponse.postValue(Resource.Success(apiServiceResp.body()!!))
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
                                term,
                                "10"
                            ).let {
                                if (it.isSuccessful) {
                                    if (term == "short_term") tracksMonthResponse.postValue(Resource.Success(it.body()!!))
                                    if (term == "medium_term") tracksSixMonthsResponse.postValue(Resource.Success(it.body()!!))
                                    if (term == "long_term") tracksLifetimeResponse.postValue(Resource.Success(it.body()!!))
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else errorResponse.postValue(it.message())
                            }
                        }
                        else errorResponse.postValue(accServiceResp.message())
                    }
                }
                else -> errorResponse.postValue(apiServiceResp.message())
            }
        }
    }
}