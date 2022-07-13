package com.bustasirio.triskl.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayHistory(
    @SerializedName("played_at")
    val playedAt: String,
    val track: Track
) : Parcelable