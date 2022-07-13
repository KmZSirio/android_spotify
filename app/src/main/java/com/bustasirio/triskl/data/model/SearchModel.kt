package com.bustasirio.triskl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchModel(
    val albums: SearchResultAlbums,
    val artists: SearchResultArtists,
    val tracks: SearchResultTracks,
    val playlists: SearchResultPlaylists,
    val shows: SearchResultShows,
    val episodes: SearchResultEpisodes
    ) : Parcelable
