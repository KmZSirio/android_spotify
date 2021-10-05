package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlists(
    val href: String,
    val items: MutableList<Playlist>,
    val limit: Int,
    val offset: Int,
    val total: Int
) : Parcelable