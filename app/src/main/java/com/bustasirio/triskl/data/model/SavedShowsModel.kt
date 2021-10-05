package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class SavedShowsModel(
    val href: String,
    @SerializedName("items")
    val savedShows: MutableList<SavedShow>,
    val limit: Int,
    val offset: Int,
    val total: Int
)