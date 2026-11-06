package com.example.ecommerceapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.databinding.ActivityHomeFragmentBinding
import com.example.ecommerceapp.helpers.LocalHelper

class HomeFragment : Fragment() {

    private var _binding: ActivityHomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupButtons()
        setupSearch()
        setupCategoryChips()
        setupLanguageSelector()
    }

    // ---------------------------------------------------------------
    // RecyclerViews — now pull from ProductRepository so data is shared
    // ---------------------------------------------------------------
    private fun setupRecyclerViews() {
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommended.adapter = ProductAdapter(ProductRepository.recommended)

        binding.rvTrending.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTrending.adapter = ProductAdapter(ProductRepository.trending)

        binding.rvPopular.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.rvPopular.adapter = ProductAdapter(ProductRepository.popular)
    }

    // ---------------------------------------------------------------
    // Buttons — "See All" now navigates to ProductListActivity
    // ---------------------------------------------------------------
    private fun setupButtons() {
        binding.btnMenu.setOnClickListener {
            Toast.makeText(requireContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Profile clicked", Toast.LENGTH_SHORT).show()
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
    // Language selector (unchanged behaviour)
    // ---------------------------------------------------------------
    private fun setupLanguageSelector() {
        binding.btnLanguage.setOnClickListener {
            val languages = arrayOf("English", "isiXhosa", "isiZulu")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Language / Khetha ulwimi")
                .setItems(languages) { _, which ->
                    val langCode = when (which) {
                        0 -> "en"
                        1 -> "xh"
                        2 -> "zu"
                        else -> "en"
                    }
                    LocalHelper.saveLanguage(requireContext(), langCode)
                    LocalHelper.setLocale(requireContext())
                    requireActivity().recreate()
                }
                .show()
        }
    }

    // ---------------------------------------------------------------
    // Search (unchanged behaviour for now)
    // ---------------------------------------------------------------
    private fun setupSearch() {
        binding.btnSearchIcon.setOnClickListener {
            val searchText = binding.etSearch.text.toString().trim()
            if (searchText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter something to search", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Searching for: $searchText", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------------------------------------------------------
    // Category chips — open the full list filtered by chip
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
            Intent(requireContext(), ProductListActivity::class.java).apply {
                putExtra(ProductListActivity.EXTRA_CATEGORY, category)
            }
        )
    }

    private fun openChip(chipLabel: String) {
        startActivity(
            Intent(requireContext(), ProductListActivity::class.java).apply {
                putExtra(ProductListActivity.EXTRA_CATEGORY, ProductListActivity.CATEGORY_ALL)
                putExtra(ProductListActivity.EXTRA_CHIP, chipLabel)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}