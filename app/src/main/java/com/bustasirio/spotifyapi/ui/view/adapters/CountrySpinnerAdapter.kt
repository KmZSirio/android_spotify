package com.bustasirio.spotifyapi.ui.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bustasirio.spotifyapi.R
import java.util.*

class CountrySpinnerAdapter(context: Context, markets: List<String>) : ArrayAdapter<String>(context, 0, markets) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {

        val market = getItem(position)

        val locale = Locale("", market!!)

        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        view.findViewById<TextView>(R.id.tvSpinnerItem).text = if (position == 0 ) "Worldwide" else locale.displayCountry

        return view
    }
}