package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class SavedEpisode(
    @SerializedName("added_at")
    val addedAt: String,
    val episode: Episode
)