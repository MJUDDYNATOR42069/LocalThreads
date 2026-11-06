package com.example.ecommerceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.databinding.ActivityHomeFragmentBinding

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
    }

    private fun setupRecyclerViews() {
        // Recommended products
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Trending products
        binding.rvTrending.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Popular products
        binding.rvPopular.layoutManager =
            GridLayoutManager(requireContext(), 2)
    }

    private fun setupButtons() {
        binding.btnMenu.setOnClickListener {
            Toast.makeText(requireContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Language clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllRecommended.setOnClickListener {
            Toast.makeText(requireContext(), "Recommended products", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllTrending.setOnClickListener {
            Toast.makeText(requireContext(), "Trending products", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllPopular.setOnClickListener {
            Toast.makeText(requireContext(), "Popular products", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun setupCategoryChips() {
        binding.chipAll.setOnClickListener {
            Toast.makeText(requireContext(), "Showing all products", Toast.LENGTH_SHORT).show()
        }

        binding.chipNew.setOnClickListener {
            Toast.makeText(requireContext(), "Showing new products", Toast.LENGTH_SHORT).show()
        }

        binding.chipSecondHand.setOnClickListener {
            Toast.makeText(requireContext(), "Showing second-hand products", Toast.LENGTH_SHORT).show()
        }

        binding.chipTshirts.setOnClickListener {
            Toast.makeText(requireContext(), "Showing T-Shirts", Toast.LENGTH_SHORT).show()
        }

        binding.chipDresses.setOnClickListener {
            Toast.makeText(requireContext(), "Showing dresses", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
