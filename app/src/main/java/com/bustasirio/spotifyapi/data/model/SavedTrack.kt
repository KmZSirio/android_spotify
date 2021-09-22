package com.bustasirio.spotifyapi.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SavedTrack(
    val added_at: String,
    val track: Track
) : Parcelable