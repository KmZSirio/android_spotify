package com.bustasirio.spotifyapi.data.providers

import com.bustasirio.spotifyapi.data.models.AuthorizationModel
import retrofit2.Call
import retrofit2.http.*

interface SpotifyClient {

    @Headers(
        "Accept: application/json"
    )
    @POST("/api/token")
    @FormUrlEncoded
    fun getAccessToken(
        @Header("Authorization") authorization : String,
        @Field("grant_type") grantType : String,
        @Field("code") code : String,
        @Field("redirect_uri") redirectUri : String
    ) : Call<AuthorizationModel>
}