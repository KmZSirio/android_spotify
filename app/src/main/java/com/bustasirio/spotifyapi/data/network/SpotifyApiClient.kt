package com.bustasirio.spotifyapi.data.network

import com.bustasirio.spotifyapi.data.model.*
import okhttp3.RequestBody
import org.json.JSONObject
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
        @Query("limit") limit: String,
        @Query("offset") offset: String
    ): Response<PlaylistsModel>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("/v1/me")
    suspend fun getCurrentUserProfile(
        @Header("Authorization") authorizationWithToken: String
    ): Response<User>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    ) // /v1/playlists/4hMHVf43VaRJuaF7SI30tu/tracks
    @GET
    suspend fun getPlaylistItems(
        @Url url: String,
        @Header("Authorization") authorizationWithToken: String,
        @Query("limit") limit: String,
        @Query("offset") offset: String
    ): Response<TracksListModel>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("/v1/me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") authorizationWithToken: String
    ): Response<RecentlyPlayedModel>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST
    suspend fun postPlaylist(
        @Url url: String,
        @Header("Authorization") authorizationWithToken: String,
        @Body body: RequestBody
    ): Response<Playlist>


    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("/v1/playlists/{playlist_id}/tracks")
    suspend fun postItemsToPlaylist(
        @Header("Authorization") authorizationWithToken: String,
        @Path("playlist_id") playlistId: String,
        @Body body: RequestBody
    ): Response<Snapshot>
}