package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val href: String,
    val icons: List<Image>,
    val id: String,
    val name: String
) : Parcelable