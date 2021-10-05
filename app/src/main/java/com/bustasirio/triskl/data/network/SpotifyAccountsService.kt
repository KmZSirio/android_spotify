package com.bustasirio.triskl.data.network

import com.bustasirio.triskl.data.model.AuthorizationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class SpotifyAccountsService @Inject constructor(private val api: SpotifyAccountsClient) {

    suspend fun getAuth(
        authorization: String,
        grantType: String,
        code: String,
        redirectUri: String
    ): Response<AuthorizationModel> {
        return withContext(Dispatchers.IO) {
            api.getAccessToken(
                authorization,
                grantType,
                code,
                redirectUri
            )
        }
    }


    suspend fun getNewToken(
        authorization: String,
        grantType: String,
        refreshToken: String,
        redirectUri: String
    ): Response<AuthorizationModel> {
        return withContext(Dispatchers.IO) {
            api.getNewToken(
                authorization,
                grantType,
                refreshToken,
                redirectUri
            )
        }
    }
}