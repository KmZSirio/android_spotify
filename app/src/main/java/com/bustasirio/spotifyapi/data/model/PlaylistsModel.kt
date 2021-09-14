package com.bustasirio.spotifyapi.data.model

import com.google.gson.annotations.SerializedName

data class PlaylistsModel(
    val href: String,
    @SerializedName("items")
    val playlists: List<Playlist>,
    val limit: Int,
    val offset: Int,
    val total: Int
)