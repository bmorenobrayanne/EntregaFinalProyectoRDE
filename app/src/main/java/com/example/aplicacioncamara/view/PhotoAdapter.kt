package com.example.aplicacioncamara.view

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacioncamara.R
import com.example.aplicacioncamara.model.PhotoEntity

class PhotoAdapter(private var photos: List<PhotoEntity>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPhoto)
        val textViewCoords: TextView = itemView.findViewById(R.id.textViewCoords)
        val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        holder.imageView.setImageURI(Uri.parse(photo.uri))
        holder.textViewCoords.text = "Lat: ${photo.latitude}, Lon: ${photo.longitude}"
        holder.textViewDateTime.text = photo.dateTime
    }

    override fun getItemCount(): Int = photos.size

    fun setPhotos(newPhotos: List<PhotoEntity>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}