package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class TopTracksModel(
    val href: String,
    @SerializedName("items")
    val tracks: List<Track>,
    val limit: Int,
    val offset: Int,
    val total: Int
)