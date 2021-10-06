package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants
import com.bustasirio.triskl.core.Constants.Companion.QUERY_GRID_SIZE
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.model.CategoryPlaylistsModel
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    var page = 0
    private var playlists: CategoryPlaylistsModel? = null

    val playlistsResponse: MutableLiveData<Resource<CategoryPlaylistsModel>> = MutableLiveData()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    val categoryId = MutableLiveData<String>()

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    val country = MutableLiveData<String>()

    fun safeFetchCategoryPlaylists() {
        playlistsResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchCategoryPlaylists()
            else playlistsResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> playlistsResponse.postValue(Resource.Error("network"))
                else -> playlistsResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    // * CATEGORY'S PLAYLISTS
    private fun fetchCategoryPlaylists() = viewModelScope.launch {

        apiService.getCategoryPlaylists(
            authorizationWithToken.value!!,
            categoryId.value ?: "",
            if (country.value!!.isEmpty()) null else country.value!!,
            "$QUERY_GRID_SIZE",
            "${page * QUERY_GRID_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> playlistsSuccessful(apiServiceResp)
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

                            apiService.getCategoryPlaylists(
                                authWithToken,
                                categoryId.value ?: "",
                                if (country.value!!.isEmpty()) null else country.value!!,
                                "$QUERY_GRID_SIZE",
                                "${page * QUERY_GRID_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    playlistsSuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else playlistsResponse.postValue(Resource.Error(it.code().toString()))
                            }
                        }
                        else playlistsResponse.postValue(Resource.Error(accServiceResp.code().toString()))
                    }
                }
                else -> playlistsResponse.postValue(Resource.Error(apiServiceResp.code().toString()))
            }
        }
    }

    private fun playlistsSuccessful(response: Response<CategoryPlaylistsModel>) {
        page++
        if (playlists == null) {
            playlists = response.body()
        } else {
            val oldItems = playlists?.playlists?.items
            val newItems = response.body()?.playlists?.items
            oldItems?.addAll(newItems!!)
        }
        playlistsResponse.postValue(Resource.Success(playlists ?: response.body()!!))
    }
}