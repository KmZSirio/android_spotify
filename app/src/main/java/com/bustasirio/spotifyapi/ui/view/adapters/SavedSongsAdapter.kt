package com.bustasirio.spotifyapi.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Item
import com.bustasirio.spotifyapi.data.model.SavedTrack
import com.squareup.picasso.Picasso

class SavedSongsAdapter :
    RecyclerView.Adapter<SavedSongsAdapter.SavedSongsViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<SavedTrack>() {
        override fun areItemsTheSame(oldItem: SavedTrack, newItem: SavedTrack): Boolean {
            return oldItem.track == newItem.track
        }

        override fun areContentsTheSame(oldItem: SavedTrack, newItem: SavedTrack): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SavedSongsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SavedSongsViewHolder(
            layoutInflater.inflate(
                R.layout.playlist_track_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SavedSongsViewHolder, position: Int) {

        val savedTrack = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNamePlaylistTrackItem.text = savedTrack.track.name
            holder.tvArtistNamePlaylistTrackItem.text = savedTrack.track.artists[0].name

            if (savedTrack.track.album.images.isNotEmpty()) {
                Picasso.get().load(savedTrack.track.album.images[0].url)
                    .into(holder.ivAlbumPlaylistTrackItem)
            } else {
                holder.ivAlbumPlaylistTrackItem.setImageResource(R.drawable.playlist_cover)
            }

            setOnClickListener {
                onItemClickListener?.let { it(savedTrack) }
            }
        }

        holder.ibMenuPlaylistTrackItem.apply {
            setOnClickListener {
                onMenuClickListener?.let { it(savedTrack) }
            }
        }
    }

    private var onMenuClickListener: ((SavedTrack) -> Unit)? = null
    fun setOnMenuClickListener(listener: (SavedTrack) -> Unit) {
        onMenuClickListener = listener
    }

    private var onItemClickListener: ((SavedTrack) -> Unit)? = null
    fun setOnItemClickListener(listener: (SavedTrack) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class SavedSongsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivAlbumPlaylistTrackItem: ImageView = view.findViewById(R.id.ivAlbumPlaylistTrackItem)
        val tvNamePlaylistTrackItem: TextView = view.findViewById(R.id.tvNamePlaylistTrackItem)
        val tvArtistNamePlaylistTrackItem: TextView =
            view.findViewById(R.id.tvArtistNamePlaylistTrackItem)
        val ibMenuPlaylistTrackItem: ImageButton =
            view.findViewById(R.id.ibMenuPlaylistTrackItem)
    }
}