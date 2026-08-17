package com.example.movinbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movinbuddy.data.Item

class ItemRowAdapter(
    private val items: List<Item>,
    private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemRowAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.itemNameTextView)
        val summary: TextView = view.findViewById(R.id.itemSummaryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        val context = holder.itemView.context
        holder.summary.text = CommentTextUtils.buildSummary(context, item.comment, item.photos.size)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
