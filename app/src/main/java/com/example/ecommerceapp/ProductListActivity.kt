package com.example.ecommerceapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ecommerceapp.databinding.ActivityProductListBinding

class ProductListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_CHIP = "extra_chip"
        const val CATEGORY_RECOMMENDED = "recommended"
        const val CATEGORY_TRENDING = "trending"
        const val CATEGORY_POPULAR = "popular"
        const val CATEGORY_ALL = "all"
    }

    private lateinit var binding: ActivityProductListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: CATEGORY_ALL
        val chip = intent.getStringExtra(EXTRA_CHIP)

        // Update the title
        binding.tvTitle.text = when {
            chip != null -> chip
            category == CATEGORY_RECOMMENDED -> "Recommended for you"
            category == CATEGORY_TRENDING -> "Trending in your area"
            category == CATEGORY_POPULAR -> "Popular in this category"
            else -> "All Products"
        }

        // Back arrow
        binding.btnBack.setOnClickListener { finish() }

        // Populate the list
        val products = if (chip != null) {
            ProductRepository.byChip(chip)
        } else {
            ProductRepository.byCategory(category)
        }

        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvProducts.adapter = ProductAdapter(products)

        if (products.isEmpty()) {
            Toast.makeText(this, "No products in this section yet", Toast.LENGTH_SHORT).show()
        }
    }
}
