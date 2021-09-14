package com.bustasirio.spotifyapi.data.network

import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.TopArtistsModel
import com.bustasirio.spotifyapi.data.model.TopTracksModel
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
}