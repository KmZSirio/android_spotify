package com.bustasirio.spotifyapi.data.network

import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface SpotifyAccountsClient {

    @Headers(
        "Accept: application/json"
    )
    @POST("/api/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Header("Authorization") authorization : String,
        @Field("grant_type") grantType : String,
        @Field("code") code : String,
        @Field("redirect_uri") redirectUri : String
    ) : Response<AuthorizationModel>
}