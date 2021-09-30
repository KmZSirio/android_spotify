package com.bustasirio.spotifyapi.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class BottomSheetViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    val completedResponse = MutableLiveData<Int>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun addToQueue(uri: String) = viewModelScope.launch {

        apiService.postToQueue(
            authorizationWithToken.value!!,
            uri
        ).let { apiServiceResp ->
            when (val code = apiServiceResp.code()) {
                204 -> completedResponse.value = code
                403 -> completedResponse.value = code
                404 -> completedResponse.value = code
                401 -> {

                    accountsService.getNewToken(
                        auth.value!!,
                        "refresh_token",
                        refreshToken.value!!,
                        Constants.REDIRECT_URI
                    ).let { accServiceResp ->
                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.postToQueue(
                                authWithToken,
                                uri
                            ).let {
                                when (it.code()) {
                                    204 -> completedResponse.value = it.code()
                                    403 -> completedResponse.value = it.code()
                                    404 -> completedResponse.value = it.code()
                                    else -> errorResponse.postValue(it.code())
                                }
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