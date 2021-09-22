package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Episode(
    val audio_preview_url: String,
    val description: String,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val is_playable: Boolean,
    val language: String,
    val languages: List<String>,
    val name: String,
    val release_date: String,
    val release_date_precision: String,
    val show: Show,
    val type: String,
    val uri: String
) : Parcelable