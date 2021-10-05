package com.bustasirio.triskl.data.network

import com.bustasirio.triskl.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class SpotifyApiService @Inject constructor(private val api: SpotifyApiClient) {

    suspend fun getTopArtists(
        auth: String,
        limit: String
    ): Response<TopArtistsModel> {
        return withContext(Dispatchers.IO) {
            api.getTopArtists(
                auth,
                limit
            )
        }
    }

    suspend fun getTopTracks(
        auth: String,
        timeRange: String,
        limit: String
    ): Response<TopTracksModel> {
        return withContext(Dispatchers.IO) {
            api.getTopTracks(
                auth,
                timeRange,
                limit
            )
        }
    }

    suspend fun getCurrentUserPlaylists(
        auth: String,
        limit: String,
        offset: String
    ): Response<PlaylistsModel> {
        return withContext(Dispatchers.IO) {
            api.getCurrentUserPlaylists(
                auth,
                limit,
                offset
            )
        }
    }

    suspend fun getCurrentUserProfile(
        auth: String
    ): Response<User> {
        return withContext(Dispatchers.IO) {
            api.getCurrentUserProfile(
                auth
            )
        }
    }

    suspend fun getPlaylistItems(
        url: String,
        auth: String,
        limit: String,
        offset: String
    ): Response<TracksListModel> {
        return withContext(Dispatchers.IO) {
            api.getPlaylistItems(
                url,
                auth,
                limit,
                offset
            )
        }
    }

    suspend fun getRecentlyPlayed(
        auth: String
    ): Response<RecentlyPlayedModel> {
        return withContext(Dispatchers.IO) {
            api.getRecentlyPlayed(
                auth
            )
        }
    }

    suspend fun postPlaylist(
        userId: String,
        auth: String,
        body: RequestBody
    ) : Response<Playlist> {
        return withContext(Dispatchers.IO) {
            api.postPlaylist(
                userId,
                auth,
                body
            )
        }
    }

    suspend fun postItemsToPlaylist(
        auth: String,
        playlistId: String,
        body: RequestBody
    ) : Response<Snapshot> {
        return withContext(Dispatchers.IO) {
            api.postItemsToPlaylist(
                auth,
                playlistId,
                body
            )
        }
    }

    suspend fun getLikedSongs(
        auth: String,
        limit: String,
        offset: String
    ) : Response<SavedTracksModel> {
        return withContext(Dispatchers.IO) {
            api.getLikedSongs(
                auth,
                limit,
                offset
            )
        }
    }

    suspend fun getYourEpisodes(
        auth: String,
        limit: String,
        offset: String
    ) : Response<SavedEpisodesModel> {
        return withContext(Dispatchers.IO) {
            api.getYourEpisodes(
                auth,
                limit,
                offset
            )
        }
    }

    suspend fun getSavedShows(
        auth: String,
        limit: String,
        offset: String
    ) : Response<SavedShowsModel> {
        return withContext(Dispatchers.IO) {
            api.getSavedShows(
                auth,
                limit,
                offset
            )
        }
    }

    suspend fun getCategories(
        auth: String,
        country: String?,
        locale: String,
        limit: String,
        offset: String
    ) : Response<CategoryModel> {
        return withContext(Dispatchers.IO) {
            api.getCategories(
                auth,
                country,
                locale,
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
        auth: String,
        country: String?,
        locale: String,
        timestamp: String,
        limit: String,
        offset: String
    ) : Response<FeaturedPlaylistsModel> {
        return withContext(Dispatchers.IO) {
            api.getFeaturedPlaylists(
                auth,
                country,
                locale,
                timestamp,
                limit,
                offset
            )
        }
    }

    suspend fun putPlaylistDetails(
        auth: String,
        playlistId: String,
        body: RequestBody
    ) : Response<Unit> {
        return withContext(Dispatchers.IO) {
            api.putPlaylistDetails(
                auth,
                playlistId,
                body
            )
        }
    }

    suspend fun postToQueue(
        auth: String,
        uri: String
    ) : Response<Unit> {
        return withContext(Dispatchers.IO) {
            api.postToQueue(
                auth,
                uri
            )
        }
    }
}