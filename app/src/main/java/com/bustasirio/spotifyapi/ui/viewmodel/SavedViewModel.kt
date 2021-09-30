package com.bustasirio.spotifyapi.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bustasirio.spotifyapi.core.Constants
import com.bustasirio.spotifyapi.data.model.*
import com.bustasirio.spotifyapi.data.network.SpotifyAccountsService
import com.bustasirio.spotifyapi.data.network.SpotifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val apiService: SpotifyApiService,
    private val accountsService: SpotifyAccountsService
) : ViewModel() {

    var page = 0
    private var items: TracksListModel? = null
    private var songs: SavedTracksModel? = null
    private var episodes: SavedEpisodesModel? = null
    private var shows: SavedShowsModel? = null

    val tracksResponse = MutableLiveData<TracksListModel>()
    val songsResponse = MutableLiveData<SavedTracksModel>()
    val episodesResponse = MutableLiveData<SavedEpisodesModel>()
    val showsResponse = MutableLiveData<SavedShowsModel>()

    val errorResponse = MutableLiveData<Int>()
    val newTokensResponse = MutableLiveData<AuthorizationModel>()
    val loading = MutableLiveData<Boolean>()

    val tracksUrl = MutableLiveData<String>()

    val authorizationWithToken = MutableLiveData<String>() // "$tokenType $accessToken"
    val auth = MutableLiveData<String>() // prefs

    // * To renew token
    val refreshToken = MutableLiveData<String>() // prefs

    // * PLAYLISTS
    fun fetchPlaylistItems() = viewModelScope.launch {
        loading.postValue(true)

        apiService.getPlaylistItems(
            tracksUrl.value ?: "",
            authorizationWithToken.value!!,
            "${Constants.QUERY_SIZE}",
            "${page * Constants.QUERY_SIZE}"
        ).let { apiServiceResp ->
            when {
                apiServiceResp.isSuccessful -> tracksSuccessful(apiServiceResp)
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

                            apiService.getPlaylistItems(
                                tracksUrl.value!!,
                                authWithToken,
                                "${Constants.QUERY_SIZE}",
                                "${page * Constants.QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    tracksSuccessful(it)
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

    // * SONGS
    fun fetchSavedSongs() = viewModelScope.launch {
        loading.postValue(true)

        apiService.getLikedSongs(
            authorizationWithToken.value!!,
            "${Constants.QUERY_SIZE}",
            "${page * Constants.QUERY_SIZE}"
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
                                "${Constants.QUERY_SIZE}",
                                "${page * Constants.QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    songsSuccessful(it)
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

    // * EPISODES
    fun fetchSavedEpisodes() = viewModelScope.launch {
        loading.postValue(true)

        apiService.getYourEpisodes(
            authorizationWithToken.value!!,
            "${Constants.QUERY_SIZE}",
            "${page * Constants.QUERY_SIZE}"
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
                                "${Constants.QUERY_SIZE}",
                                "${page * Constants.QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    episodesSuccessful(it)
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

    // * SHOWS
    fun fetchSavedShows() = viewModelScope.launch {
        loading.postValue(true)

        apiService.getSavedShows(
            authorizationWithToken.value!!,
            "${Constants.QUERY_SIZE}",
            "${page * Constants.QUERY_SIZE}"
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
                                "${Constants.QUERY_SIZE}",
                                "${page * Constants.QUERY_SIZE}"
                            ).let {
                                if (it.isSuccessful) {
                                    showsSuccessful(it)
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

    private fun tracksSuccessful(response: Response<TracksListModel>) {
        page++
        if (items == null) {
            items = response.body()
        } else {
            val oldItems = items?.items
            val newItems = response.body()?.items
            oldItems?.addAll(newItems!!)
        }
        tracksResponse.postValue(items ?: response.body())
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
        songsResponse.postValue(songs ?: response.body())
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
        episodesResponse.postValue(episodes ?: response.body())
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
        showsResponse.postValue(shows ?: response.body())
    }
}