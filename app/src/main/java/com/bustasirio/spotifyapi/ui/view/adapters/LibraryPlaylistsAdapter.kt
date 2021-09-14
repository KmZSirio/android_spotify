package com.bustasirio.spotifyapi.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Playlist
import com.squareup.picasso.Picasso

class LibraryPlaylistsAdapter(private val playlists: List<Playlist>) :
    RecyclerView.Adapter<LibraryPlaylistsAdapter.LibraryPlaylistsHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LibraryPlaylistsHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LibraryPlaylistsHolder(
            layoutInflater.inflate(
                R.layout.your_library_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: LibraryPlaylistsHolder,
        position: Int
    ) {
        var type = "playlist"
        type = if( playlists[position].type == type)
            "Playlist"
        else
            playlists[position].type

        holder.tvNameYourLibraryItem.text = playlists[position].name
        holder.tvDescriptionYourLibraryItem.text = type + " • " + playlists[position].owner.display_name
        if (playlists[position].images.isNotEmpty()) {
            Picasso.get().load(playlists[position].images[0].url).into(holder.ivPlaylistYourLibraryItem)
        } else {
            holder.ivPlaylistYourLibraryItem.setImageResource(R.drawable.no_artist)
        }
    }

    override fun getItemCount(): Int = playlists.size

    class LibraryPlaylistsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivPlaylistYourLibraryItem: ImageView = view.findViewById(R.id.ivPlaylistYourLibraryItem)
        val tvNameYourLibraryItem: TextView = view.findViewById(R.id.tvNameYourLibraryItem)
        val tvDescriptionYourLibraryItem: TextView =
            view.findViewById(R.id.tvDescriptionYourLibraryItem)
    }
}