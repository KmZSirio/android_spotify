package com.bustasirio.spotifyapi.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.CategoryModel
import com.bustasirio.spotifyapi.data.model.MarketsModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    val marketsResponse = MutableLiveData<MarketsModel>()

    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    // * MARKETS
    fun fetchMarkets() = viewModelScope.launch {
        apiService.getAvailableMarkets(
            authorizationWithToken.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> marketsResponse.postValue(apiServiceResp.body())
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        auth.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        Constants.REDIRECT_URI
                    ).let { accServiceResp ->

                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.getAvailableMarkets(
                                authWithToken
                            ).let {
                                if (it.isSuccessful) {
                                    marketsResponse.postValue(it.body())
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