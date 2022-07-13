package com.bustasirio.triskl.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val collaborative: Boolean,
    val description: String?,
    @SerializedName("external_urls")
    val externalUrls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val public: Boolean?,
    val tracks: Tracks,
    val type: String,
    val uri: String
) : Parcelable