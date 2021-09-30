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
class EditViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    val changedResponse = MutableLiveData<Boolean>()
    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val requestBody = MutableLiveData<RequestBody>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun editPlaylistDetails(playlistId: String) = viewModelScope.launch {

        apiService.putPlaylistDetails(
            authorizationWithToken.value!!,
            playlistId,
            requestBody.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.code() == 200 -> changedResponse.value = true
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

                            apiService.putPlaylistDetails(
                                authWithToken,
                                playlistId,
                                requestBody.value!!
                            ).let {
                                if (it.code() == 200) {
                                    changedResponse.value = true
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