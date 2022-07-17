package com.bustasirio.triskl.ui.view.adapters

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.CircleTransformation
import com.bustasirio.triskl.core.convertDpToPx
import com.bustasirio.triskl.core.getCroppedBitmap
import com.bustasirio.triskl.data.model.Artist
import com.bustasirio.triskl.data.model.Item
import com.bustasirio.triskl.data.model.Track
import com.squareup.picasso.Picasso

class ArtistsResultAdapter :
    RecyclerView.Adapter<ArtistsResultAdapter.ArtistsResultViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldArtist: Artist, newArtist: Artist): Boolean {
            return oldArtist == newArtist
        }

        override fun areContentsTheSame(oldArtist: Artist, newArtist: Artist): Boolean {
            return oldArtist == newArtist
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArtistsResultViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ArtistsResultViewHolder(
            layoutInflater.inflate(
                R.layout.artist_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ArtistsResultViewHolder, position: Int) {

        val artist = differ.currentList[position]

        holder.itemView.apply {
            holder.tvNameArtistName.text = artist.name

            if (!artist.images.isNullOrEmpty()) {
                Picasso.get().load(artist.images[0].url).transform(CircleTransformation())
                    .into(holder.ivImageArtistItem)
            } else {
                val icon = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.artist_cover
                )
                holder.ivImageArtistItem.setImageBitmap(getCroppedBitmap(icon))
            }

            setOnClickListener {
                onItemClickListener?.let { it(artist) }
            }
        }
    }

    private var onItemClickListener: ((Artist) -> Unit)? = null
    fun setOnItemClickListener(listener: (Artist) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ArtistsResultViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivImageArtistItem: ImageView = view.findViewById(R.id.ivImageArtistItem)
        val tvNameArtistName: TextView = view.findViewById(R.id.tvNameArtistName)
    }
}