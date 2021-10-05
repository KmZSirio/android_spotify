package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class RecentlyPlayedModel(
    val href: String,
    @SerializedName("items")
    val play_histories: MutableList<PlayHistory>,
    val limit: Int
)