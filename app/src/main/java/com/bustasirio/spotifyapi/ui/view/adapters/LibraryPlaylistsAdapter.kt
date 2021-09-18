package com.bustasirio.spotifyapi.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Playlist
import com.squareup.picasso.Picasso

class LibraryPlaylistsAdapter :
    RecyclerView.Adapter<LibraryPlaylistsAdapter.LibraryPlaylistsViewHolder>() {

    val itemPosition = MutableLiveData<Int>()

    private val differCallback = object : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.href == newItem.href
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LibraryPlaylistsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LibraryPlaylistsViewHolder(
            layoutInflater.inflate(
                R.layout.your_library_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LibraryPlaylistsViewHolder, position: Int) {

        val playlist = differ.currentList[position]

        var type = "playlist"
        type = if (playlist.type == type)
            "Playlist"
        else
            playlist.type


        holder.itemView.apply {
            holder.tvNameYourLibraryItem.text = playlist.name
            holder.tvDescriptionYourLibraryItem.text =
                type + " • " + playlist.owner.display_name
            if (playlist.images.isNotEmpty()) {
                Picasso.get().load(playlist.images[0].url)
                    .into(holder.ivPlaylistYourLibraryItem)
            } else {
                holder.ivPlaylistYourLibraryItem.setImageResource(R.drawable.no_artist)
            }

            holder.itemView.setOnClickListener {
                itemPosition.postValue(position)
            }
        }


    }

    override fun getItemCount(): Int = differ.currentList.size

    class LibraryPlaylistsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivPlaylistYourLibraryItem: ImageView = view.findViewById(R.id.ivPlaylistYourLibraryItem)
        val tvNameYourLibraryItem: TextView = view.findViewById(R.id.tvNameYourLibraryItem)
        val tvDescriptionYourLibraryItem: TextView =
            view.findViewById(R.id.tvDescriptionYourLibraryItem)
    }
}