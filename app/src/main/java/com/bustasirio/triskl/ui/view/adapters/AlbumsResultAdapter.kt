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

class AlbumsResultAdapter :
    RecyclerView.Adapter<AlbumsResultAdapter.AlbumsResultViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldAlbum: Album, newAlbum: Album): Boolean {
            return oldAlbum.href == newAlbum.href
        }

        override fun areContentsTheSame(oldAlbum: Album, newAlbum: Album): Boolean {
            return oldAlbum == newAlbum
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumsResultViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return AlbumsResultViewHolder(
            layoutInflater.inflate(
                R.layout.album_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AlbumsResultViewHolder, position: Int) {
        val album = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNameAlbumItem.text = album.name
            holder.tvArtistAlbumItem.text = album.artists[0].name
            holder.tvYearAlbumItem.text = album.releaseDate.take(4)
            if (album.images.isNotEmpty()) {
                Picasso.get().load(album.images[0].url)
                    .into(holder.ivImageAlbumItem)
            }
            else holder.ivImageAlbumItem.setImageResource(R.drawable.playlist_cover)

            setOnClickListener { onItemClickListener?.let { it(album) } }
        }
    }

    private var onItemClickListener: ((Album) -> Unit)? = null

    fun setOnItemClickListener(listener: (Album) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class AlbumsResultViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivImageAlbumItem: ImageView = view.findViewById(R.id.ivImageAlbumItem)
        val tvNameAlbumItem: TextView = view.findViewById(R.id.tvNameAlbumItem)
        val tvArtistAlbumItem: TextView = view.findViewById(R.id.tvArtistAlbumItem)
        val tvYearAlbumItem: TextView = view.findViewById(R.id.tvYearAlbumItem)
    }
}