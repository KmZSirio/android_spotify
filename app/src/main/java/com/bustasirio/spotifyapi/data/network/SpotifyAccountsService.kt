package com.bustasirio.spotifyapi.data.network

import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SpotifyAccountsService @Inject constructor(private val api: SpotifyAccountsClient) {
    suspend fun getAuth(
        authorization: String,
        grantType: String,
        code: String,
        redirectUri: String
    ) : Response<AuthorizationModel> {
        return withContext(Dispatchers.IO) {
            api.getAccessToken(
                authorization,
                grantType,
                code,
                redirectUri
            )
        }
    }
}