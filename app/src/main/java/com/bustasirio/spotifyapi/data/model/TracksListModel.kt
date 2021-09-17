package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TracksListModel(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val offset: Int,
    val total: Int
) : Parcelable