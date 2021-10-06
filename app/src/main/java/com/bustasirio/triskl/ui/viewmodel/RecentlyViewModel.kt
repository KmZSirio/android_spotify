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
import com.bustasirio.triskl.data.model.RecentlyPlayedModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RecentlyViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    private var items: RecentlyPlayedModel? = null

    val recentlyResponse: MutableLiveData<Resource<RecentlyPlayedModel>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeFetchRecentlyPlayed() {
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchRecentlyPlayed()
            else recentlyResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> recentlyResponse.postValue(Resource.Error("network"))
                else -> recentlyResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    private fun fetchRecentlyPlayed() = viewModelScope.launch {
        apiService.getRecentlyPlayed(
            authorizationWithToken.value!!
        ).let { apiServiceResp ->
            when {
                apiServiceResp.code() == 200 -> recentlySuccessful(apiServiceResp)
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
                            apiService.getRecentlyPlayed(
                                authWithToken,
                            ).let {
                                if (it.isSuccessful) {
                                    recentlySuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else recentlyResponse.postValue(Resource.Error(it.code().toString()))
                            }
                        }
                        else recentlyResponse.postValue(Resource.Error(accServiceResp.code().toString()))
                    }
                }
                else ->recentlyResponse.postValue(Resource.Error(apiServiceResp.code().toString()))
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
        recentlyResponse.postValue(Resource.Success(items ?: response.body()!!))
    }
}