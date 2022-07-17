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
import com.bustasirio.triskl.data.model.SavedEpisode
import com.squareup.picasso.Picasso

class SavedEpisodeAdapter :
    RecyclerView.Adapter<SavedEpisodeAdapter.SavedEpisodeViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<SavedEpisode>() {
        override fun areItemsTheSame(oldItem: SavedEpisode, newItem: SavedEpisode): Boolean {
            return oldItem.episode == newItem.episode
        }

        override fun areContentsTheSame(oldItem: SavedEpisode, newItem: SavedEpisode): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SavedEpisodeViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SavedEpisodeViewHolder(
            layoutInflater.inflate(
                R.layout.episode_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SavedEpisodeViewHolder, position: Int) {
        val savedEpisode = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNamePodcastItem.text = savedEpisode.episode.name
            holder.tvPublisherPodcastItem.text = savedEpisode.episode.show?.publisher ?: ""
            holder.tvDescriptionPodcastItem.text = savedEpisode.episode.description

            if (savedEpisode.episode.images.isNotEmpty()) {
                Picasso.get().load(savedEpisode.episode.images[0].url)
                    .into(holder.ivPodcastItem)
            } else {
                holder.ivPodcastItem.setImageResource(R.drawable.playlist_cover)
            }

            setOnClickListener {
                onItemClickListener?.let { it(savedEpisode) }
            }
        }
    }

    private var onItemClickListener: ((SavedEpisode) -> Unit)? = null

    fun setOnItemClickListener(listener: (SavedEpisode) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class SavedEpisodeViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val ivPodcastItem: ImageView = view.findViewById(R.id.ivEpisodeItem)
        val tvNamePodcastItem: TextView = view.findViewById(R.id.tvNameEpisodeItem)
        val tvPublisherPodcastItem: TextView = view.findViewById(R.id.tvPublisherEpisodeItem)
        val tvDescriptionPodcastItem: TextView = view.findViewById(R.id.tvDescriptionEpisodeItem)
    }
}