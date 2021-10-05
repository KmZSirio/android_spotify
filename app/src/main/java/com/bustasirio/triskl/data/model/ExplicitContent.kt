package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExplicitContent(
    val filter_enabled: Boolean,
    val filter_locked: Boolean
) : Parcelable