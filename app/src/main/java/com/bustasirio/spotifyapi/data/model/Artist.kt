package com.bustasirio.spotifyapi.data.model

data class Artist(
    val external_urls: ExternalUrls,
    val genres: List<String>,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val popularity: Int,
    val type: String,
    val uri: String
)