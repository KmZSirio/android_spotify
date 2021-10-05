package com.bustasirio.triskl.core

class Constants {
    companion object {
        const val QUERY_SIZE = 20
        const val QUERY_GRID_SIZE = 50
        const val REDIRECT_URI = "triskl://callback"
        const val CLIENT_ID = "151eea6d2b7a4630ace40005f01405b6"
        const val SCOPE =
            "playlist-read-private " +
            "playlist-read-collaborative " +
            "user-top-read " +
            "user-read-email " +
            "user-read-recently-played " +
            "playlist-modify-public " +
            "playlist-modify-private " +
            "user-library-read " +
            "user-modify-playback-state"
    }
}