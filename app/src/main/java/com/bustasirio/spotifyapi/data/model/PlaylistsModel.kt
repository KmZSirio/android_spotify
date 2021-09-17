package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistsModel(
    val href: String,
    @SerializedName("items")
    val playlists: List<Playlist>,
    val limit: Int,
    val offset: Int,
    val total: Int
) : Parcelable