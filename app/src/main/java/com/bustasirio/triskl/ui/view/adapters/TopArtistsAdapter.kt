package com.bustasirio.triskl.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.R
import com.bustasirio.triskl.data.model.Artist
import com.squareup.picasso.Picasso

class TopArtistsAdapter(private val artists: List<Artist>) :
    RecyclerView.Adapter<TopArtistsAdapter.TopArtistsHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopArtistsHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TopArtistsHolder(layoutInflater.inflate(R.layout.square_artist_item, parent, false))
    }

    override fun onBindViewHolder(
        holder: TopArtistsHolder,
        position: Int
    ) {
        holder.tvArtistName.text = artists[position].name
        if (artists[position].images != null) {
            if (artists[position].images!!.isNotEmpty())
                Picasso.get().load(artists[position].images!![0].url).into(holder.ivArtistImage)
        } else {
            holder.ivArtistImage.setImageResource(R.drawable.artist_cover)
        }
    }

    override fun getItemCount(): Int = artists.size

    class TopArtistsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivArtistImage: ImageView = view.findViewById(R.id.ivArtistImageSquareArtist)
        val tvArtistName: TextView = view.findViewById(R.id.tvArtistNameSquareArtist)
    }
}