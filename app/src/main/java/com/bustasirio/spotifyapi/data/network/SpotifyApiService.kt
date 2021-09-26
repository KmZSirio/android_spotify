package com.bustasirio.spotifyapi.data.network

import com.bustasirio.spotifyapi.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class SpotifyApiService @Inject constructor(private val api: SpotifyApiClient) {

    suspend fun getTopArtists(
        authorizationWithToken: String,
        limit: String
    ): Response<TopArtistsModel> {
        return withContext(Dispatchers.IO) {
            api.getTopArtists(
                authorizationWithToken,
                limit
            )
        }
    }

    suspend fun getTopTracks(
        authorizationWithToken: String,
        timeRange: String,
        limit: String
    ): Response<TopTracksModel> {
        return withContext(Dispatchers.IO) {
            api.getTopTracks(
                authorizationWithToken,
                timeRange,
                limit
            )
        }
    }

    suspend fun getCurrentUserPlaylists(
        authorizationWithToken: String,
        limit: String,
        offset: String
    ): Response<PlaylistsModel> {
        return withContext(Dispatchers.IO) {
            api.getCurrentUserPlaylists(
                authorizationWithToken,
                limit,
                offset
            )
        }
    }

    suspend fun getCurrentUserProfile(
        authorizationWithToken: String
    ): Response<User> {
        return withContext(Dispatchers.IO) {
            api.getCurrentUserProfile(
                authorizationWithToken
            )
        }
    }

    suspend fun getPlaylistItems(
        url: String,
        authorizationWithToken: String,
        limit: String,
        offset: String
    ): Response<TracksListModel> {
        return withContext(Dispatchers.IO) {
            api.getPlaylistItems(
                url,
                authorizationWithToken,
                limit,
                offset
            )
        }
    }

    suspend fun getRecentlyPlayed(
        authorizationWithToken: String
    ): Response<RecentlyPlayedModel> {
        return withContext(Dispatchers.IO) {
            api.getRecentlyPlayed(
                authorizationWithToken
            )
        }
    }

    suspend fun postPlaylist(
        url: String,
        authorizationWithToken: String,
        body: RequestBody
    ) : Response<Playlist> {
        return withContext(Dispatchers.IO) {
            api.postPlaylist(
                url,
                authorizationWithToken,
                body
            )
        }
    }

    suspend fun postItemsToPlaylist(
        authorizationWithToken: String,
        playlistId: String,
        body: RequestBody
    ) : Response<Snapshot> {
        return withContext(Dispatchers.IO) {
            api.postItemsToPlaylist(
                authorizationWithToken,
                playlistId,
                body
            )
        }
    }

    suspend fun getLikedSongs(
        authorizationWithToken: String,
        limit: String,
        offset: String
    ) : Response<SavedTracksModel> {
        return withContext(Dispatchers.IO) {
            api.getLikedSongs(
                authorizationWithToken,
                limit,
                offset
            )
        }
    }

    suspend fun getYourEpisodes(
        authorizationWithToken: String,
        limit: String,
        offset: String
    ) : Response<SavedEpisodesModel> {
        return withContext(Dispatchers.IO) {
            api.getYourEpisodes(
                authorizationWithToken,
                limit,
                offset
            )
        }
    }

    suspend fun getSavedShows(
        authorizationWithToken: String,
        limit: String,
        offset: String
    ) : Response<SavedShowsModel> {
        return withContext(Dispatchers.IO) {
            api.getSavedShows(
                authorizationWithToken,
                limit,
                offset
            )
        }
    }

    suspend fun getCategories(
        authorizationWithToken: String,
        country: String?,
        limit: String,
        offset: String
    ) : Response<CategoryModel> {
        return withContext(Dispatchers.IO) {
            api.getCategories(
                authorizationWithToken,
                country,
                limit,
                offset
            )
        }
    }

    suspend fun getCategoryPlaylists(
        auth: String,
        categoryId: String,
        country: String?,
        limit: String,
        offset: String
    ) : Response<CategoryPlaylistsModel> {
        return withContext(Dispatchers.IO) {
            api.getCategoryPlaylists(
                auth,
                categoryId,
                country,
                limit,
                offset
            )
        }
    }

    suspend fun getAvailableMarkets(
        auth: String
    ) : Response<MarketsModel> {
        return withContext(Dispatchers.IO) {
            api.getAvailableMarkets(
                auth
            )
        }
    }

    suspend fun getFeaturedPlaylists(
        authorizationWithToken: String,
        country: String?,
        timestamp: String,
        limit: String,
        offset: String
    ) : Response<FeaturedPlaylistsModel> {
        return withContext(Dispatchers.IO) {
            api.getFeaturedPlaylists(
                authorizationWithToken,
                country,
                timestamp,
                limit,
                offset
            )
        }
    }
}