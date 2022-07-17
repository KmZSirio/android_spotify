package com.bustasirio.triskl.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArtistAlbumsModel(
    val href: String,
    @SerializedName("items")
    val albums: List<Album>,
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
) : Parcelable
