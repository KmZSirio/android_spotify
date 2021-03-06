package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class SavedEpisodesModel(
    val href: String,
    @SerializedName("items")
    val savedEpisodes: MutableList<SavedEpisode>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)