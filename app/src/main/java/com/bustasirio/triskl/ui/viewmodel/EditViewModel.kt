package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    val changedResponse: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val requestBody = MutableLiveData<RequestBody>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeEditPlaylist(playlistId: String) {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) editPlaylistDetails(playlistId)
            else changedResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when (t) {
                is IOException -> changedResponse.postValue(Resource.Error("network"))
                else -> changedResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    private fun editPlaylistDetails(playlistId: String) = viewModelScope.launch {

        apiService.putPlaylistDetails(
            authorizationWithToken.value!!,
            playlistId,
            requestBody.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.code() == 200 -> changedResponse.postValue(Resource.Success(true))
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
                                    changedResponse.postValue(Resource.Success(true))
                                    newTokensResponse.postValue(accServiceResp.body())
                                } else changedResponse.postValue(Resource.Error(it.message()))
                            }
                        } else changedResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> changedResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }
}