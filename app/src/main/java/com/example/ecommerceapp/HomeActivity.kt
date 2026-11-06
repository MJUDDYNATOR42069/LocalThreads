package com.example.ecommerceapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.databinding.ActivityHomeFragmentBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityHomeFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupButtons()
        setupSearch()
        setupCategoryChips()
    }

    private fun setupRecyclerViews() {
        // Recommended products
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Trending products
        binding.rvTrending.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Popular products
        binding.rvPopular.layoutManager =
            GridLayoutManager(this, 2)
    }

    private fun setupButtons() {
        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLanguage.setOnClickListener {
            Toast.makeText(this, "Language clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnProfile.setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllRecommended.setOnClickListener {
            Toast.makeText(this, "Recommended products", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllTrending.setOnClickListener {
            Toast.makeText(this, "Trending products", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllPopular.setOnClickListener {
            Toast.makeText(this, "Popular products", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearch() {
        binding.btnSearchIcon.setOnClickListener {
            val searchText = binding.etSearch.text.toString().trim()
            if (searchText.isEmpty()) {
                Toast.makeText(this, "Please enter something to search", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Searching for: $searchText", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCategoryChips() {
        binding.chipAll.setOnClickListener {
            Toast.makeText(this, "Showing all products", Toast.LENGTH_SHORT).show()
        }

        binding.chipNew.setOnClickListener {
            Toast.makeText(this, "Showing new products", Toast.LENGTH_SHORT).show()
        }

        binding.chipSecondHand.setOnClickListener {
            Toast.makeText(this, "Showing second-hand products", Toast.LENGTH_SHORT).show()
        }

        binding.chipTshirts.setOnClickListener {
            Toast.makeText(this, "Showing T-Shirts", Toast.LENGTH_SHORT).show()
        }

        binding.chipDresses.setOnClickListener {
            Toast.makeText(this, "Showing dresses", Toast.LENGTH_SHORT).show()
        }
    }
}
