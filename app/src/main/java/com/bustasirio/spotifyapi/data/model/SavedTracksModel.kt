package com.bustasirio.spotifyapi.data.model

import com.google.gson.annotations.SerializedName

data class SavedTracksModel(
    val href: String,
    @SerializedName("items")
    val savedTracks: List<SavedTrack>,
    val limit: Int,
    val offset: Int,
    val total: Int
)