package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Show(
    val description: String,
    val explicit: Boolean,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val languages: List<String>,
    val media_type: String,
    val name: String,
    val publisher: String,
    val total_episodes: Int,
    val type: String,
    val uri: String
) : Parcelable