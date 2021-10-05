package com.bustasirio.triskl.core

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
object PlayerSingleton : Application() {
    private var player: MediaPlayer? = null

    fun getPlayerInstance(context: Context, url: String): MediaPlayer? {
        if (player == null) {
            player = MediaPlayer()
            player = MediaPlayer.create(context, Uri.parse(url))
        } else {
            if (player!!.isPlaying) player!!.stop()
            player = MediaPlayer.create(context, Uri.parse(url))
        }
        return player
    }
}