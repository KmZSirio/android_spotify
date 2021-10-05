package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayHistory(
    val played_at: String,
    val track: Track
) : Parcelable