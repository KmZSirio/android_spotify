package com.bustasirio.spotifyapi.data.model

data class Playlist(
    val collaborative: Boolean,
    val description: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val public: Boolean,
    val tracks: Tracks,
    val type: String,
    val uri: String
)