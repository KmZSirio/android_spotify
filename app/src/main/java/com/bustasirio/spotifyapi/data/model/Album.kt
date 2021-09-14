package com.bustasirio.spotifyapi.data.model

data class Album(
    val album_type: String,
    val artists: List<Artist>,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val total_tracks: Int,
    val type: String,
    val uri: String
)