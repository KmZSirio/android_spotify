package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarketsModel(
    val markets: MutableList<String>
) : Parcelable