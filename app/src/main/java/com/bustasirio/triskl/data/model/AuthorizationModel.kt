package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class AuthorizationModel(
    @SerializedName("access_token")
    var accessToken : String,
    @SerializedName("token_type")
    var tokenType : String,
    @SerializedName("expires_in")
    var expiresIn : Int,
    @SerializedName("refresh_token")
    var refreshToken : String?,
    @SerializedName("scope")
    var scope : String
)