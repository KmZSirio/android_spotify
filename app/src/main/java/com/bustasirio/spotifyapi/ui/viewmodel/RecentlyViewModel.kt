package com.bustasirio.spotifyapi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.RecentlyPlayedModel
import com.bustasirio.spotifyapi.data.model.TracksListModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class RecentlyViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    private var items: RecentlyPlayedModel? = null

    val recentlyResponse = MutableLiveData<RecentlyPlayedModel>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val authorizationBasic = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun fetchRecentlyPlayed() = viewModelScope.launch {
        apiService.getRecentlyPlayed(
            authorizationWithToken.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.code() == 200 -> recentlySuccessful(apiServiceResp)
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        authorizationBasic.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        Constants.REDIRECT_URI
                    ).let { accServiceResp ->

                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"
                            apiService.getRecentlyPlayed(
                                authWithToken,
                            ).let {
                                if (it.isSuccessful) {
                                    recentlySuccessful(it)
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

    private fun recentlySuccessful(response: Response<RecentlyPlayedModel>) {
        if (items == null) {
            items = response.body()
        } else {
            val oldItems = items?.play_histories
            val newItems = response.body()?.play_histories
            oldItems?.addAll(newItems!!)
        }
        recentlyResponse.postValue(items ?: response.body())
    }
}