package com.example.movinbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movinbuddy.data.Section

class SectionAdapter(
    private val sections: List<Section>,
    private val onClick: (Section) -> Unit,
    private val onLongClick: (Section) -> Unit
) : RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.sectionNameTextView)
        val summary: TextView = view.findViewById(R.id.sectionSummaryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        holder.name.text = section.displayName
        val itemsWithData = section.items.count { it.comment.isNotBlank() || it.photos.isNotEmpty() }
        val totalPhotos = section.items.sumOf { it.photos.size }
        holder.summary.text = holder.itemView.context.getString(
            R.string.section_summary_format, itemsWithData, section.items.size, totalPhotos
        )
        holder.itemView.setOnClickListener { onClick(section) }
        holder.itemView.setOnLongClickListener {
            onLongClick(section)
            true
        }
    }

    override fun getItemCount(): Int = sections.size
}
