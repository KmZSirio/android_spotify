package com.bustasirio.triskl.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    @SerializedName("external_urls")
    val externalUrls: ExternalUrls,
    val followers: Followers,
    val genres: List<String>?,
    val href: String,
    val id: String,
    val images: List<Image>?,
    val name: String,
    val popularity: Int,
    val type: String,
    val uri: String
) : Parcelable