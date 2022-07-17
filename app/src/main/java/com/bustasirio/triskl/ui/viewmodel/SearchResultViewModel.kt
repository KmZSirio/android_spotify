package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants
import com.bustasirio.triskl.core.ESL
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.model.SearchModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    val searchResponse: MutableLiveData<Resource<SearchModel>> = MutableLiveData()

    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>()

    val limit = "14"

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    val query = MutableLiveData<String>()

    fun safeFetchQuery() {
        searchResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchQuery()
            else searchResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchResponse.postValue(Resource.Error("network"))
                else -> searchResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    private fun fetchQuery() = viewModelScope.launch {

        apiService.search(
            authorizationWithToken.value!!,
            query.value!!,
            Constants.SEARCH_TYPE_PARAMS,
            limit
        ). let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful ->
                    searchResponse.postValue(Resource.Success(apiServiceResp.body()!!))
                apiServiceResp.code() == 401 -> {

                    accountsService.getNewToken(
                        ESL.ESL,
                        "refresh_token",
                        refreshToken.value!!,
                        Constants.REDIRECT_URI
                    ).let { accServiceResp ->

                        if (accServiceResp.isSuccessful && accServiceResp.code() == 200) {
                            val authWithToken =
                                "${accServiceResp.body()!!.tokenType} ${accServiceResp.body()!!.accessToken}"

                            apiService.search(
                                authWithToken,
                                query.value!!,
                                Constants.SEARCH_TYPE_PARAMS,
                                limit
                            ).let {
                                if (it.isSuccessful) {
                                    searchResponse.postValue(Resource.Success(it.body()!!))
                                    newTokensResponse.postValue(accServiceResp.body())
                                } else searchResponse.postValue(Resource.Error(it.code().toString()))
                            }
                        } else searchResponse.postValue(Resource.Error(accServiceResp.code().toString()))
                    }
                } else -> searchResponse.postValue(Resource.Error(apiServiceResp.code().toString()))
            }
        }
    }
}