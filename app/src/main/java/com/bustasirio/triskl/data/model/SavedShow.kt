package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SavedShow(
    val added_at: String,
    val show: Show
) : Parcelable