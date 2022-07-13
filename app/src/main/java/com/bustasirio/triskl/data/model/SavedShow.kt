package com.bustasirio.triskl.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SavedShow(
    @SerializedName("added_at")
    val addedAt: String,
    val show: Show
) : Parcelable