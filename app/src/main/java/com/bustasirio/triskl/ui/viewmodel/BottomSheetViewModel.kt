package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class BottomSheetViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    val completedResponse: MutableLiveData<Resource<Int>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeAddToQueue(uri: String) {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) addToQueue(uri)
            else completedResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> completedResponse.postValue(Resource.Error("Network Failure"))
                else -> completedResponse.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun addToQueue(uri: String) = viewModelScope.launch {

        apiService.postToQueue(
            authorizationWithToken.value!!,
            uri
        ).let { apiServiceResp ->
            when (val code = apiServiceResp.code()) {
                204, 403, 404 -> completedResponse.postValue(Resource.Success(code))
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
                                    204, 403, 404 -> completedResponse.postValue(Resource.Success(it.code()))
                                    else -> completedResponse.postValue(Resource.Error(it.message()))
                                }
                            }
                        }
                        else completedResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> completedResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }
}