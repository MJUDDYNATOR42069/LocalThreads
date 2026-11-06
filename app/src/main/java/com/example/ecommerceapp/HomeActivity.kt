package com.example.ecommerceapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.databinding.ActivityHomeFragmentBinding
import com.example.ecommerceapp.helpers.LocalHelper

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeFragmentBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupButtons()
        setupSearch()
        setupCategoryChips()
        setupLanguageSelector()
    }

    // ---------------------------------------------------------------
    // RecyclerViews — same data source as HomeFragment
    // ---------------------------------------------------------------
    private fun setupRecyclerViews() {
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommended.adapter = ProductAdapter(ProductRepository.recommended)

        binding.rvTrending.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTrending.adapter = ProductAdapter(ProductRepository.trending)

        binding.rvPopular.layoutManager =
            GridLayoutManager(this, 2)
        binding.rvPopular.adapter = ProductAdapter(ProductRepository.popular)
    }

    // ---------------------------------------------------------------
    // Buttons — "See All" navigates to ProductListActivity
    // ---------------------------------------------------------------
    private fun setupButtons() {
        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnProfile.setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllRecommended.setOnClickListener {
            openProductList(ProductListActivity.CATEGORY_RECOMMENDED)
        }

        binding.tvSeeAllTrending.setOnClickListener {
            openProductList(ProductListActivity.CATEGORY_TRENDING)
        }

        binding.tvSeeAllPopular.setOnClickListener {
            openProductList(ProductListActivity.CATEGORY_POPULAR)
        }
    }

    // ---------------------------------------------------------------
    // Language selector
    // ---------------------------------------------------------------
    private fun setupLanguageSelector() {
        binding.btnLanguage.setOnClickListener {
            val languages = arrayOf("English", "isiXhosa", "isiZulu")
            AlertDialog.Builder(this)
                .setTitle("Select Language / Khetha ulwimi")
                .setItems(languages) { _, which ->
                    val langCode = when (which) {
                        0 -> "en"
                        1 -> "xh"
                        2 -> "zu"
                        else -> "en"
                    }
                    LocalHelper.saveLanguage(this, langCode)
                    LocalHelper.setLocale(this)
                    recreate()
                }
                .show()
        }
    }

    // ---------------------------------------------------------------
    // Search
    // ---------------------------------------------------------------
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

    // ---------------------------------------------------------------
    // Category chips
    // ---------------------------------------------------------------
    private fun setupCategoryChips() {
        binding.chipAll.setOnClickListener {
            openProductList(ProductListActivity.CATEGORY_ALL)
        }

        binding.chipNew.setOnClickListener {
            openChip("New")
        }

        binding.chipSecondHand.setOnClickListener {
            openChip("Second-hand")
        }

        binding.chipTshirts.setOnClickListener {
            openChip("T-Shirts")
        }

        binding.chipDresses.setOnClickListener {
            openChip("Dresses")
        }
    }

    // ---------------------------------------------------------------
    // Navigation helpers
    // ---------------------------------------------------------------
    private fun openProductList(category: String) {
        startActivity(
            Intent(this, ProductListActivity::class.java).apply {
                putExtra(ProductListActivity.EXTRA_CATEGORY, category)
            }
        )
    }

    private fun openChip(chipLabel: String) {
        startActivity(
            Intent(this, ProductListActivity::class.java).apply {
                putExtra(ProductListActivity.EXTRA_CATEGORY, ProductListActivity.CATEGORY_ALL)
                putExtra(ProductListActivity.EXTRA_CHIP, chipLabel)
            }
        )
    }
}