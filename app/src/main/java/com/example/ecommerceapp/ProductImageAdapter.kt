package com.example.ecommerceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductImageAdapter(
    private val imageUrls: List<String>
) : RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.ivProductImage)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image, parent, false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        position: Int
    ) {

        val imageUrl = imageUrls[position]

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}