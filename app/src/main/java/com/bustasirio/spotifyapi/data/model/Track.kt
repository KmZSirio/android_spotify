package com.bustasirio.spotifyapi.data.model

data class Track(
    val album: Album,
    val artists: List<Artist>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val href: String,
    val id: String,
    val is_local: Boolean,
    val name: String,
    val preview_url: String,
    val track_number: Int,
    val type: String,
    val uri: String
)