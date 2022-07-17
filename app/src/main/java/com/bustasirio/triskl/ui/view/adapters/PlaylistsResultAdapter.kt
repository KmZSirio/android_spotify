package com.bustasirio.triskl.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.R
import com.bustasirio.triskl.data.model.Album
import com.bustasirio.triskl.data.model.Playlist
import com.squareup.picasso.Picasso

class PlaylistsResultAdapter :
    RecyclerView.Adapter<PlaylistsResultAdapter.PlaylistsResultViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldPlaylist: Playlist, newPlaylist: Playlist): Boolean {
            return oldPlaylist.href == newPlaylist.href
        }

        override fun areContentsTheSame(oldPlaylist: Playlist, newPlaylist: Playlist): Boolean {
            return oldPlaylist == newPlaylist
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistsResultViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlaylistsResultViewHolder(
            layoutInflater.inflate(
                R.layout.playlist_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlaylistsResultViewHolder, position: Int) {
        val playlist = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNamePlaylistItem.text = playlist.name
            if (playlist.images.isNotEmpty()) {
                Picasso.get().load(playlist.images[0].url)
                    .into(holder.ivImagePlaylistItem)
            }
            else holder.ivImagePlaylistItem.setImageResource(R.drawable.playlist_cover)

            setOnClickListener { onItemClickListener?.let { it(playlist) } }
        }
    }

    private var onItemClickListener: ((Playlist) -> Unit)? = null

    fun setOnItemClickListener(listener: (Playlist) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class PlaylistsResultViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivImagePlaylistItem: ImageView = view.findViewById(R.id.ivImagePlaylistItem)
        val tvNamePlaylistItem: TextView = view.findViewById(R.id.tvNamePlaylistItem)
    }
}