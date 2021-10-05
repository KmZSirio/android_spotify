package com.bustasirio.triskl.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.core.Constants
import com.bustasirio.triskl.core.Constants.Companion.QUERY_SIZE
import com.bustasirio.triskl.core.Resource
import com.bustasirio.triskl.core.hasInternetConnection
import com.bustasirio.triskl.data.model.*
import com.bustasirio.triskl.data.network.SpotifyAccountsService
import com.bustasirio.triskl.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    app: Application,
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : AndroidViewModel(app) {

    var page = 0
    private var songs: SavedTracksModel? = null
    private var episodes: SavedEpisodesModel? = null
    private var shows: SavedShowsModel? = null

    val songsResponse: MutableLiveData<Resource<SavedTracksModel>> = MutableLiveData()
    val episodesResponse: MutableLiveData<Resource<SavedEpisodesModel>> = MutableLiveData()
    val showsResponse: MutableLiveData<Resource<SavedShowsModel>> = MutableLiveData()

    val newTokensResponse = MutableLiveData<AuthorizationModel>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    fun safeFetchSavedSongs() {
        songsResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchSavedSongs()
            else songsResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> songsResponse.postValue(Resource.Error("network"))
                else -> songsResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    fun safeFetchSavedEpisodes() {
        episodesResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchSavedEpisodes()
            else episodesResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> episodesResponse.postValue(Resource.Error("network"))
                else -> episodesResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    fun safeFetchSavedShows() {
        showsResponse.postValue(Resource.Loading())
        val connectivityManager = getApplication<MyApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        try {
            if (hasInternetConnection(connectivityManager)) fetchSavedShows()
            else showsResponse.postValue(Resource.Error("internet"))
        } catch (t: Throwable) {
            when(t) {
                is IOException -> showsResponse.postValue(Resource.Error("network"))
                else -> showsResponse.postValue(Resource.Error("conversion"))
            }
        }
    }

    // * SONGS
    private fun fetchSavedSongs() = viewModelScope.launch {

        apiService.getLikedSongs(
            authorizationWithToken.value!!,
            "$QUERY_SIZE",
            "${page * QUERY_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> songsSuccessful(apiServiceResp)
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

                            apiService.getLikedSongs(
                                authWithToken,
                                "$QUERY_SIZE",
                                "${page * QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    songsSuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else songsResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else songsResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> songsResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }

    // * EPISODES
    private fun fetchSavedEpisodes() = viewModelScope.launch {

        apiService.getYourEpisodes(
            authorizationWithToken.value!!,
            "$QUERY_SIZE",
            "${page * QUERY_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> episodesSuccessful(apiServiceResp)
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

                            apiService.getYourEpisodes(
                                authWithToken,
                                "$QUERY_SIZE",
                                "${page * QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    episodesSuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else episodesResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else episodesResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> episodesResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }

    // * SHOWS
    private fun fetchSavedShows() = viewModelScope.launch {

        apiService.getSavedShows(
            authorizationWithToken.value!!,
            "$QUERY_SIZE",
            "${page * QUERY_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> showsSuccessful(apiServiceResp)
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

                            apiService.getSavedShows(
                                authWithToken,
                                "$QUERY_SIZE",
                                "${page * QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    showsSuccessful(it)
                                    newTokensResponse.postValue(accServiceResp.body())
                                }
                                else showsResponse.postValue(Resource.Error(it.message()))
                            }
                        }
                        else showsResponse.postValue(Resource.Error(accServiceResp.message()))
                    }
                }
                else -> showsResponse.postValue(Resource.Error(apiServiceResp.message()))
            }
        }
    }

    private fun songsSuccessful(response: Response<SavedTracksModel>) {
        page++
        if (songs == null) {
            songs = response.body()
        } else {
            val oldItems = songs?.savedTracks
            val newItems = response.body()?.savedTracks
            oldItems?.addAll(newItems!!)
        }
        songsResponse.postValue(Resource.Success(songs ?: response.body()!!))
    }

    private fun episodesSuccessful(response: Response<SavedEpisodesModel>) {
        page++
        if (episodes == null) {
            episodes = response.body()
        } else {
            val oldItems = episodes?.savedEpisodes
            val newItems = response.body()?.savedEpisodes
            oldItems?.addAll(newItems!!)
        }
        episodesResponse.postValue(Resource.Success(episodes ?: response.body()!!))
    }

    private fun showsSuccessful(response: Response<SavedShowsModel>) {
        page++
        if (shows == null) {
            shows = response.body()
        } else {
            val oldItems = shows?.savedShows
            val newItems = response.body()?.savedShows
            oldItems?.addAll(newItems!!)
        }
        showsResponse.postValue(Resource.Success(shows ?: response.body()!!))
    }
}