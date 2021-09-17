package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalUrls(
    val spotify: String
) : Parcelable