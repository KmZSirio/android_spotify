package com.bustasirio.triskl.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// * Parcelize and Parcelable to pass it through intents

@Parcelize
data class TopArtistsModel(
    val href: String,
    @SerializedName("items")
    val artists: List<Artist>,
    val limit: Int,
    val offset: Int,
    val total: Int
) : Parcelable