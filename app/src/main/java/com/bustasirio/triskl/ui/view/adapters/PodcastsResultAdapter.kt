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
import com.bustasirio.triskl.data.model.Show
import com.squareup.picasso.Picasso

class PodcastsResultAdapter :
    RecyclerView.Adapter<PodcastsResultAdapter.PodcastsResultViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Show>() {
        override fun areItemsTheSame(oldShow: Show, newShow: Show): Boolean {
            return oldShow.href == newShow.href
        }

        override fun areContentsTheSame(oldShow: Show, newShow: Show): Boolean {
            return oldShow == newShow
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PodcastsResultViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PodcastsResultViewHolder(
            layoutInflater.inflate(
                R.layout.podcast_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PodcastsResultViewHolder, position: Int) {
        val podcast = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNamePodcastItem.text = podcast.name
            holder.tvPublisherPodcastItem.text = podcast.publisher
            if (podcast.images.isNotEmpty()) {
                Picasso.get().load(podcast.images[0].url)
                    .into(holder.ivImagePodcastItem)
            }
            else holder.ivImagePodcastItem.setImageResource(R.drawable.artist_cover)

            setOnClickListener { onItemClickListener?.let { it(podcast) } }
        }
    }

    private var onItemClickListener: ((Show) -> Unit)? = null

    fun setOnItemClickListener(listener: (Show) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class PodcastsResultViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivImagePodcastItem: ImageView = view.findViewById(R.id.ivImagePodcastItem)
        val tvNamePodcastItem: TextView = view.findViewById(R.id.tvNamePodcastItem)
        val tvPublisherPodcastItem: TextView = view.findViewById(R.id.tvPublisherPodcastItem)
    }
}