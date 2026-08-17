package com.example.movinbuddy

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movinbuddy.data.Item

sealed class ReviewRow {
    data class SectionHeader(val title: String) : ReviewRow()
    data class ItemRow(val item: Item) : ReviewRow()
}

class ReviewAdapter(private val rows: List<ReviewRow>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val THUMBNAIL_TARGET_PX = 160
    }

    class HeaderViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view as TextView
    }

    class ItemViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.reviewThumbnailImageView)
        val name: TextView = view.findViewById(R.id.reviewItemNameTextView)
        val detail: TextView = view.findViewById(R.id.reviewItemDetailTextView)
    }

    override fun getItemViewType(position: Int): Int = when (rows[position]) {
        is ReviewRow.SectionHeader -> TYPE_HEADER
        is ReviewRow.ItemRow -> TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(inflater.inflate(R.layout.item_review_section_header, parent, false))
        } else {
            ItemViewHolder(inflater.inflate(R.layout.item_review_item_row, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is ReviewRow.SectionHeader -> (holder as HeaderViewHolder).title.text = row.title
            is ReviewRow.ItemRow -> {
                val itemHolder = holder as ItemViewHolder
                val item = row.item
                itemHolder.name.text = item.name
                val context = itemHolder.itemView.context
                itemHolder.detail.text = CommentTextUtils.buildSummary(context, item.comment, item.photos.size)
                val firstPhoto = item.photos.firstOrNull()
                if (firstPhoto != null) {
                    itemHolder.thumbnail.setImageBitmap(BitmapUtils.decodeSampledBitmap(firstPhoto.filePath, THUMBNAIL_TARGET_PX, THUMBNAIL_TARGET_PX))
                } else {
                    itemHolder.thumbnail.setImageResource(android.R.drawable.ic_menu_camera)
                }
            }
        }
    }

    override fun getItemCount(): Int = rows.size
}
