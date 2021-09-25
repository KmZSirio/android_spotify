package com.bustasirio.spotifyapi.ui.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Category
import com.squareup.picasso.Picasso
import kotlin.random.Random

class CategoryAdapter :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.href == newItem.href
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CategoryViewHolder(
            layoutInflater.inflate(
                R.layout.category_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = differ.currentList[position]

        holder.itemView.apply {
            holder.cvCategoryItem.setCardBackgroundColor(
                Color.argb(
                    255,
                    Random.nextInt(30, 220),
                    Random.nextInt(30, 220),
                    Random.nextInt(30, 220)
                )
            )

            holder.tvCategoryItem.text = category.name
            if (category.icons.isNotEmpty()) {
                Picasso.get().load(category.icons[0].url)
                    .into(holder.ivCategoryItem)
            }
            else holder.ivCategoryItem.setImageResource(R.drawable.playlist_cover)

            setOnClickListener { onItemClickListener?.let { it(category) } }
        }
    }

    private var onItemClickListener: ((Category) -> Unit)? = null

    fun setOnItemClickListener(listener: (Category) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size

    class CategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryItem: TextView = view.findViewById(R.id.tvCategoryItem)
        val ivCategoryItem: ImageView = view.findViewById(R.id.ivCategoryItem)
        val cvCategoryItem: CardView = view.findViewById(R.id.cvCategoryItem)
    }
}