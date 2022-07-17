package com.bustasirio.triskl.ui.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.msToMin
import com.bustasirio.triskl.data.model.Album
import com.bustasirio.triskl.data.model.Episode
import com.bustasirio.triskl.data.model.Playlist
import com.bustasirio.triskl.data.model.Show
import com.squareup.picasso.Picasso
import org.joda.time.format.ISODateTimeFormat

class EpisodesResultAdapter :
    RecyclerView.Adapter<EpisodesResultAdapter.EpisodesResultViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldEpisode: Episode, newEpisode: Episode): Boolean {
            return oldEpisode.href == newEpisode.href
        }

        override fun areContentsTheSame(oldEpisode: Episode, newEpisode: Episode): Boolean {
            return oldEpisode == newEpisode
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EpisodesResultViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EpisodesResultViewHolder(
            layoutInflater.inflate(
                R.layout.episode_result_item,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EpisodesResultViewHolder, position: Int) {
        val episode = differ.currentList[position]
// ?       •
        holder.itemView.apply {
            val string = episode.releaseDate
            val fmt: org.joda.time.format.DateTimeFormatter = ISODateTimeFormat.date()
            val date = fmt.parseLocalDate(string).toString("MMM dd, yyyy")

            holder.tvNameEpisodeResultItem.text = episode.name
            holder.tvDescriptionEpisodeResultItem.text = episode.description
            holder.tvDateEpisodeResultItem.text = "$date • ${msToMin(episode.durationMs)} min"
            if (episode.images.isNotEmpty()) {
                Picasso.get().load(episode.images[0].url)
                    .into(holder.ivImageEpisodeResultItem)
            }
            else holder.ivImageEpisodeResultItem.setImageResource(R.drawable.playlist_cover)

            setOnClickListener { onItemClickListener?.let { it(episode) } }
        }
    }

    private var onItemClickListener: ((Episode) -> Unit)? = null

    fun setOnItemClickListener(listener: (Episode) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class EpisodesResultViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivImageEpisodeResultItem: ImageView = view.findViewById(R.id.ivImageEpisodeResultItem)
        val tvNameEpisodeResultItem: TextView = view.findViewById(R.id.tvNameEpisodeResultItem)
        val tvDescriptionEpisodeResultItem: TextView = view.findViewById(R.id.tvDescriptionEpisodeResultItem)
        val tvDateEpisodeResultItem: TextView = view.findViewById(R.id.tvDateEpisodeResultItem)
    }
}