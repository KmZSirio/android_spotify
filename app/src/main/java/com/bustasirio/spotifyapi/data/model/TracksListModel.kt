package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TracksListModel(
    val href: String,
    val items: MutableList<Item>,
    val limit: Int,
    val offset: Int,
    val total: Int
) : Parcelable