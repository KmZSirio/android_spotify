package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants.Companion.QUERY_SIZE
import com.bustasirio.triskl.core.Constants.Companion.REDIRECT_URI
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.model.TracksListModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    var page = 0
    private var items: TracksListModel? = null

    val tracksResponse: MutableLiveData<Resource<TracksListModel>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val tracksUrl = MutableLiveData<String>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeFetchItems() {
        tracksResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchPlaylistItems()
            else tracksResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> tracksResponse.postValue(Resource.Error("network"))
                else -> tracksResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    private fun fetchPlaylistItems() = viewModelScope.launch {

        apiService.getPlaylistItems(
            tracksUrl.value ?: "",
            authorizationWithToken.value!!,
            "$QUERY_SIZE",
            "${page * QUERY_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> tracksSuccessful(apiServiceResp)
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

                            apiService.getPlaylistItems(
                                tracksUrl.value!!,
                                authWithToken,
                                "$QUERY_SIZE",
                                "${page * QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    tracksSuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else tracksResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else tracksResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> tracksResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }

    private fun tracksSuccessful(response: Response<TracksListModel>) {
        page++
        if (items == null) {
            items = response.body()
        } else {
            val oldItems = items?.items
            val newItems = response.body()?.items
            oldItems?.addAll(newItems!!)
        }
        tracksResponse.postValue(Resource.Success(items ?: response.body()!!))
    }
}