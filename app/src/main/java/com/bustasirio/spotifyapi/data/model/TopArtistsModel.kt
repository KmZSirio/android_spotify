package com.bustasirio.spotifyapi.data.model

import com.google.gson.annotations.SerializedName

// * Parcelize and Parcelable to pass it through intents
data class TopArtistsModel(
    val href: String,
    @SerializedName("items")
    val artists: List<Artist>,
    val limit: Int,
    val offset: Int,
    val total: Int
)
