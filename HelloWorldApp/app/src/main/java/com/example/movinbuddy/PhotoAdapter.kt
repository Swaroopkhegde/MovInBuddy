package com.example.movinbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(
    private val photoPaths: List<String>,
    private val onLongClick: (String) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val path = photoPaths[position]
        holder.image.setImageBitmap(BitmapUtils.decodeSampledBitmap(path, THUMBNAIL_TARGET_PX, THUMBNAIL_TARGET_PX))
        holder.image.setOnLongClickListener {
            onLongClick(path)
            true
        }
    }

    companion object {
        private const val THUMBNAIL_TARGET_PX = 480
    }

    override fun getItemCount(): Int = photoPaths.size
}
