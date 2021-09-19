package com.bustasirio.spotifyapi.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Item
import com.bustasirio.spotifyapi.data.model.Playlist
import com.squareup.picasso.Picasso

class PlaylistAdapter :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.track == newItem.track
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlaylistViewHolder(
            layoutInflater.inflate(
                R.layout.playlist_track_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {

        val item = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNamePlaylistTrackItem.text = item.track.name
            holder.tvArtistNamePlaylistTrackItem.text = item.track.artists[0].name

            if (item.track.album.images.isNotEmpty()) {
                Picasso.get().load(item.track.album.images[0].url)
                    .into(holder.ivAlbumPlaylistTrackItem)
            } else {
                holder.ivAlbumPlaylistTrackItem.setImageResource(R.drawable.no_artist)
            }

            setOnClickListener {
                onItemClickListener?.let { it(item) }
            }
        }
    }

    private var onItemClickListener: ((Item) -> Unit)? = null

    fun setOnItemClickListener(listener: (Item) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class PlaylistViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivAlbumPlaylistTrackItem: ImageView = view.findViewById(R.id.ivAlbumPlaylistTrackItem)
        val tvNamePlaylistTrackItem: TextView = view.findViewById(R.id.tvNamePlaylistTrackItem)
        val tvArtistNamePlaylistTrackItem: TextView =
            view.findViewById(R.id.tvArtistNamePlaylistTrackItem)
    }
}