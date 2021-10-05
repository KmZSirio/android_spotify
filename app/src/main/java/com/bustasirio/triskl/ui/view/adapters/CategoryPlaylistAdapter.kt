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
import com.bustasirio.triskl.data.model.Playlist
import com.squareup.picasso.Picasso

class CategoryPlaylistAdapter :
    RecyclerView.Adapter<CategoryPlaylistAdapter.CategoryPlaylistViewHolder>() {

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
    ): CategoryPlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CategoryPlaylistViewHolder(
            layoutInflater.inflate(
                R.layout.category_playlist_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryPlaylistViewHolder, position: Int) {
        val playlist = differ.currentList[position]

        holder.itemView.apply {
            holder.tvCategoryPlaylistItem.text = playlist.name
            if (playlist.images.isNotEmpty()) {
                Picasso.get().load(playlist.images[0].url)
                    .into(holder.ivCategoryPlaylistItem)
            }
            else holder.ivCategoryPlaylistItem.setImageResource(R.drawable.playlist_cover)

            setOnClickListener { onItemClickListener?.let { it(playlist) } }
        }
    }

    private var onItemClickListener: ((Playlist) -> Unit)? = null

    fun setOnItemClickListener(listener: (Playlist) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class CategoryPlaylistViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryPlaylistItem: TextView = view.findViewById(R.id.tvCategoryPlaylistItem)
        val ivCategoryPlaylistItem: ImageView = view.findViewById(R.id.ivCategoryPlaylistItem)
    }
}