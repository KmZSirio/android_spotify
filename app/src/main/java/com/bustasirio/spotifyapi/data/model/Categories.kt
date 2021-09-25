package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Categories(
    val href: String,
    val items: MutableList<Category>,
    val limit: Int,
    val offset: Int,
    val total: Int
) : Parcelable