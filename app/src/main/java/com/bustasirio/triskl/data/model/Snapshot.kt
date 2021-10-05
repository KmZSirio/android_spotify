package com.bustasirio.triskl.data.model

import com.google.gson.annotations.SerializedName

data class Snapshot(
    @SerializedName("snapshot_id")
    val snapshotId: String
)