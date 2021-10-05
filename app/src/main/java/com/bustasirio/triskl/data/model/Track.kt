package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val album: Album,
    val artists: List<Artist>,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val is_local: Boolean,
    val name: String,
    val popularity: Int,
    val preview_url: String?,
    val track_number: Int,
    val type: String,
    val uri: String
) : Parcelable