package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.TopArtistsModel
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

    val response = MutableLiveData<TopArtistsModel>()
    val responseError = MutableLiveData<Int>()
    val responseNewTokens = MutableLiveData<AuthorizationModel>()

    //    val limit = MutableLiveData<String>()
    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs
    private val redirectUri: String = "bustasirio://callback"

    fun fetchTopArtists() = viewModelScope.launch {
        apiService.getTopArtists(
            authorizationWithToken.value!!,
            "10"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> {
//                    Log.d("tagHomeViewModel", "Line 40. ${apiServiceResp.body()}")
                    response.postValue(apiServiceResp.body())
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
                                    response.postValue(it.body())
                                    responseNewTokens.postValue(accServiceResp.body())
                                } else {
                                    Log.d("tagHomeViewModel", "line 64")
                                    responseError.postValue(it.code())
                                }
                            }

                        } else {
                            Log.d("tagHomeViewModel", "line 70")
                            responseError.postValue(accServiceResp.code())
                        }
                    }

                }
                else -> {
                    Log.d("tagHomeViewModel", "line 77")
                    responseError.postValue(apiServiceResp.code())
                }
            }
        }
    }
}