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
import com.bustasirio.triskl.data.model.MarketsModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    val marketsResponse: MutableLiveData<Resource<MarketsModel>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeFetchMarkets() {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchMarkets()
            else marketsResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> marketsResponse.postValue(Resource.Error("network"))
                else -> marketsResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    // * MARKETS
    private fun fetchMarkets() = viewModelScope.launch {
        apiService.getAvailableMarkets(
            authorizationWithToken.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> marketsResponse.postValue(
                    Resource.Success(
                        apiServiceResp.body()!!
                    )
                )
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
                                    marketsResponse.postValue(Resource.Success(it.body()!!))
                                    newTokensResponse.postValue(accServiceResp.body())
                                } else marketsResponse.postValue(Resource.Error(it.message()))
                            }
                        } else marketsResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> marketsResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }
}