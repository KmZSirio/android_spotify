package com.bustasirio.triskl.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchResultShows(
    val href: String,
    @SerializedName("items")
    val shows: List<Show>?,
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: Int?,
    val total: Int
) : Parcelable
