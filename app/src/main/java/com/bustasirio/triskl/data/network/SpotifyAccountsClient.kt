package com.bustasirio.triskl.data.network

import com.bustasirio.triskl.data.model.AuthorizationModel
import retrofit2.Response
import retrofit2.http.*

interface SpotifyAccountsClient {

    @Headers(
        "Accept: application/json"
    )
    @POST("/api/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<AuthorizationModel>


    @Headers(
        "Accept: application/json"
    )
    @POST("/api/token")
    @FormUrlEncoded
    suspend fun getNewToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<AuthorizationModel>
}