package com.bustasirio.spotifyapi.data.model

import com.google.gson.annotations.SerializedName

data class RecentlyPlayedModel(
    val href: String,
    @SerializedName("items")
    val play_histories: MutableList<PlayHistory>,
    val limit: Int
)