package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class SavedTracksModel(
    val href: String,
    @SerializedName("items")
    val savedTracks: MutableList<SavedTrack>,
    val limit: Int,
    val offset: Int,
    val total: Int
)