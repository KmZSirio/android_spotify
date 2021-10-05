package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val country: String,
    val display_name: String,
    val email: String,
    val explicit_content: ExplicitContent,
    val external_urls: ExternalUrls,
    val followers: Followers,
    val href: String,
    val id: String,
    val images: List<Image>,
    val product: String,
    val type: String,
    val uri: String
) : Parcelable