package com.bustasirio.spotifyapi.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Item
import com.squareup.picasso.Picasso

class PlaylistAdapter(private val items: List<Item>) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder>() {

    val previewUrl = MutableLiveData<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlaylistHolder(
            layoutInflater.inflate(
                R.layout.playlist_track_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {

        holder.tvNamePlaylistTrackItem.text = items[position].track.name
        holder.tvArtistNamePlaylistTrackItem.text = items[position].track.artists[0].name

        if (items[position].track.album.images.isNotEmpty()) {
            Picasso.get().load(items[position].track.album.images[0].url)
                .into(holder.ivAlbumPlaylistTrackItem)
        } else {
            holder.ivAlbumPlaylistTrackItem.setImageResource(R.drawable.no_artist)
        }

        holder.itemView.setOnClickListener {
//            Log.d("tagLibraryPlaylistsAdapter","${playlists[position].tracks.href}")
            previewUrl.postValue(items[position].track.preview_url ?: null)
        }
    }

    override fun getItemCount(): Int = items.size

    class PlaylistHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivAlbumPlaylistTrackItem: ImageView = view.findViewById(R.id.ivAlbumPlaylistTrackItem)
        val tvNamePlaylistTrackItem: TextView = view.findViewById(R.id.tvNamePlaylistTrackItem)
        val tvArtistNamePlaylistTrackItem: TextView =
            view.findViewById(R.id.tvArtistNamePlaylistTrackItem)
    }
}