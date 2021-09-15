package com.bustasirio.spotifyapi.data.network

import com.bustasirio.spotifyapi.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        limit: String
    ): Response<PlaylistsModel> {
        return withContext(Dispatchers.IO) {
            api.getCurrentUserPlaylists(
                authorizationWithToken,
                limit
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
}