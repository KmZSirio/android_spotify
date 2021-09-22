package com.bustasirio.spotifyapi.data.model

import com.google.gson.annotations.SerializedName

data class Snapshot(
    @SerializedName("snapshot_id")
    val snapshotId: String
)