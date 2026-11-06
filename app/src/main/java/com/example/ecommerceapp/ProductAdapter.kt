package com.example.ecommerceapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerceapp.databinding.ViewHolderProductBinding
import com.example.ecommerceapp.model.Product

class ProductAdapter(private var products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewHolderProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewHolderProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.binding.apply {
            tvProductName.text = product.name
            tvProductPrice.text = "${product.price}"
            tvProductCategory.text = product.category
            
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .into(ivProductImage)

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, ProductDetailActivity::class.java).apply {
                    putExtra("product_id", product.id)
                    putExtra("product_name", product.name)
                    putExtra("product_price", product.price)
                    putExtra("product_image", product.imageUrl)
                }
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }
}
