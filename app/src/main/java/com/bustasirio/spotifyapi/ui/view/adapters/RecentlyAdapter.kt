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
import com.bustasirio.spotifyapi.data.model.PlayHistory
import com.squareup.picasso.Picasso

class RecentlyAdapter :
    RecyclerView.Adapter<RecentlyAdapter.RecentlyViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<PlayHistory>() {
        override fun areItemsTheSame(oldItem: PlayHistory, newItem: PlayHistory): Boolean {
            return oldItem.track == newItem.track
        }

        override fun areContentsTheSame(oldItem: PlayHistory, newItem: PlayHistory): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentlyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecentlyViewHolder(
            layoutInflater.inflate(
                R.layout.recently_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecentlyViewHolder, position: Int) {
        val playHistory = differ.currentList[position]

        holder.itemView.apply {
            holder.tvDateRecentlyItem.text = playHistory.played_at
            holder.tvNameRecentlyItem.text = playHistory.track.name
            holder.tvArtistRecentlyItem.text = playHistory.track.artists[0].name

            if (playHistory.track.album.images.isNotEmpty()) {
                Picasso.get().load(playHistory.track.album.images[0].url)
                    .into(holder.ivAlbumRecentlyItem)
            } else {
                holder.ivAlbumRecentlyItem.setImageResource(R.drawable.no_artist)
            }

            setOnClickListener {
                onItemClickListener?.let { it(playHistory) }
            }
        }
    }

    private var onItemClickListener: ((PlayHistory) -> Unit)? = null

    fun setOnItemClickListener(listener: (PlayHistory) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class RecentlyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivAlbumRecentlyItem: ImageView = view.findViewById(R.id.ivAlbumRecentlyItem)
        val tvDateRecentlyItem: TextView = view.findViewById(R.id.tvDateRecentlyItem)
        val tvNameRecentlyItem: TextView = view.findViewById(R.id.tvNameRecentlyItem)
        val tvArtistRecentlyItem: TextView =
            view.findViewById(R.id.tvArtistRecentlyItem)
    }
}