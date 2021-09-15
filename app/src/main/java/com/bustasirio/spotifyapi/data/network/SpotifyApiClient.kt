package com.bustasirio.spotifyapi.data.network

import com.bustasirio.spotifyapi.data.model.PlaylistsModel
import com.bustasirio.spotifyapi.data.model.TopArtistsModel
import com.bustasirio.spotifyapi.data.model.TopTracksModel
import com.bustasirio.spotifyapi.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface SpotifyApiClient {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("/v1/me/top/artists")
    suspend fun getTopArtists(
        @Header("Authorization") authorizationWithToken: String,
        @Query("limit") limit: String
    ): Response<TopArtistsModel>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("/v1/me/top/tracks")
    suspend fun getTopTracks(
        @Header("Authorization") authorizationWithToken: String,
        @Query("time_range") timeRange: String,
        @Query("limit") limit: String
    ): Response<TopTracksModel>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("/v1/me/playlists")
    suspend fun getCurrentUserPlaylists(
        @Header("Authorization") authorizationWithToken: String,
        @Query("limit") limit: String
    ): Response<PlaylistsModel>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("/v1/me")
    suspend fun getCurrentUserProfile(
        @Header("Authorization") authorizationWithToken: String
    ): Response<User>
}