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
import com.bustasirio.triskl.data.model.SavedShow
import com.squareup.picasso.Picasso

class SavedShowAdapter :
    RecyclerView.Adapter<SavedShowAdapter.SavedShowViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<SavedShow>() {
        override fun areItemsTheSame(oldItem: SavedShow, newItem: SavedShow): Boolean {
            return oldItem.show == newItem.show
        }

        override fun areContentsTheSame(oldItem: SavedShow, newItem: SavedShow): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SavedShowViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SavedShowViewHolder(
            layoutInflater.inflate(
                R.layout.episode_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SavedShowViewHolder, position: Int) {
        val savedShow = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNamePodcastItem.text = savedShow.show.name
            holder.tvPublisherPodcastItem.text = savedShow.show.publisher
            holder.tvDescriptionPodcastItem.text = savedShow.show.description

            if (savedShow.show.images.isNotEmpty()) {
                Picasso.get().load(savedShow.show.images[0].url)
                    .into(holder.ivPodcastItem)
            } else {
                holder.ivPodcastItem.setImageResource(R.drawable.playlist_cover)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class SavedShowViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val ivPodcastItem: ImageView = view.findViewById(R.id.ivEpisodeItem)
        val tvNamePodcastItem: TextView = view.findViewById(R.id.tvNameEpisodeItem)
        val tvPublisherPodcastItem: TextView = view.findViewById(R.id.tvPublisherEpisodeItem)
        val tvDescriptionPodcastItem: TextView = view.findViewById(R.id.tvDescriptionEpisodeItem)
    }
}