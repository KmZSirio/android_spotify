package com.bustasirio.triskl.ui.view.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.R
import com.bustasirio.triskl.data.model.Playlist
import com.squareup.picasso.Picasso
import kotlin.random.Random

class FeaturedAdapter :
    RecyclerView.Adapter<FeaturedAdapter.FeaturedViewHolder>() {

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
    ): FeaturedViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FeaturedViewHolder(
            layoutInflater.inflate(
                R.layout.category_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        val playlist = differ.currentList[position]

        if (playlist != null) {
            holder.itemView.apply {
                holder.cvCategoryItem.setCardBackgroundColor(
                    Color.argb(
                        255,
                        Random.nextInt(30, 220),
                        Random.nextInt(30, 220),
                        Random.nextInt(30, 220)
                    )
                )
                holder.tvCategoryItem.text = playlist.name ?: ""
                if (playlist.images.isNotEmpty()) {
                    Picasso.get().load(playlist.images[0].url)
                        .into(holder.ivCategoryItem)
                }
                else holder.ivCategoryItem.setImageResource(R.drawable.playlist_cover)

                setOnClickListener { onItemClickListener?.let { it(playlist) } }
            }
        }
    }

    private var onItemClickListener: ((Playlist) -> Unit)? = null

    fun setOnItemClickListener(listener: (Playlist) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class FeaturedViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryItem: TextView = view.findViewById(R.id.tvCategoryItem)
        val ivCategoryItem: ImageView = view.findViewById(R.id.ivCategoryItem)
        val cvCategoryItem: CardView = view.findViewById(R.id.cvCategoryItem)
    }
}