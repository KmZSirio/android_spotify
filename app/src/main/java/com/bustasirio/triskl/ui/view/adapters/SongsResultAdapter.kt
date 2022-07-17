package com.bustasirio.triskl.ui.view.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.convertDpToPx
import com.bustasirio.triskl.data.model.Item
import com.bustasirio.triskl.data.model.Track
import com.squareup.picasso.Picasso

class SongsResultAdapter :
    RecyclerView.Adapter<SongsResultAdapter.SongsResultViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldTrack: Track, newTrack: Track): Boolean {
            return oldTrack == newTrack
        }

        override fun areContentsTheSame(oldTrack: Track, newTrack: Track): Boolean {
            return oldTrack == newTrack
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongsResultViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SongsResultViewHolder(
            layoutInflater.inflate(
                R.layout.playlist_track_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongsResultViewHolder, position: Int) {

        val track = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNamePlaylistTrackItem.text = track.name
            holder.tvArtistNamePlaylistTrackItem.text = track.artists[0].name

            if (track.album.images.isNotEmpty()) {
                Picasso.get().load(track.album.images[0].url)
                    .into(holder.ivAlbumPlaylistTrackItem)
            } else {
                holder.ivAlbumPlaylistTrackItem.setImageResource(R.drawable.playlist_cover)
            }

            setOnClickListener {
                onItemClickListener?.let { it(track) }
            }
        }

        holder.ibMenuPlaylistTrackItem.apply {
            setOnClickListener {
                onMenuClickListener?.let { it(track) }
            }
        }
    }

    private var onMenuClickListener: ((Track) -> Unit)? = null
    fun setOnMenuClickListener(listener: (Track) -> Unit) {
        onMenuClickListener = listener
    }

    private var onItemClickListener: ((Track) -> Unit)? = null
    fun setOnItemClickListener(listener: (Track) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class SongsResultViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivAlbumPlaylistTrackItem: ImageView = view.findViewById(R.id.ivAlbumPlaylistTrackItem)
        val tvNamePlaylistTrackItem: TextView = view.findViewById(R.id.tvNamePlaylistTrackItem)
        val tvArtistNamePlaylistTrackItem: TextView =
            view.findViewById(R.id.tvArtistNamePlaylistTrackItem)
        val ibMenuPlaylistTrackItem: ImageButton =
            view.findViewById(R.id.ibMenuPlaylistTrackItem)
    }
}