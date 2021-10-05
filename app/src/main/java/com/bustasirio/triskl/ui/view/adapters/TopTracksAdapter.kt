package com.bustasirio.triskl.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.triskl.R
import com.bustasirio.triskl.data.model.Track
import com.squareup.picasso.Picasso

class TopTracksAdapter(private val tracks: List<Track>) :
    RecyclerView.Adapter<TopTracksAdapter.TopTracksHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopTracksHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TopTracksHolder(layoutInflater.inflate(R.layout.on_repeat_item, parent, false))
    }

    override fun onBindViewHolder(holder: TopTracksHolder, position: Int) {
        holder.tvTrackName.text = tracks[position].name
        holder.tvArtistName.text = tracks[position].artists[0].name
        if (tracks[position].album.images.isNotEmpty()) {
            Picasso.get().load(tracks[position].album.images[0].url).into(holder.ivTrackImage)
        } else {
            holder.ivTrackImage.setImageResource(R.drawable.playlist_cover)
        }
    }

    override fun getItemCount(): Int = tracks.size

    class TopTracksHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val ivTrackImage: ImageView = view.findViewById(R.id.ivTrackImageOnRepeat)
        val tvTrackName: TextView = view.findViewById(R.id.tvTrackNameOnRepeat)
        val tvArtistName: TextView = view.findViewById(R.id.tvArtistNameOnRepeat)
    }
}