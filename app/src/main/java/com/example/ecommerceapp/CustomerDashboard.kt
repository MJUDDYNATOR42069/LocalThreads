package com.example.ecommerceapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommerceapp.databinding.ActivityCustomerDashbaordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDashbaordBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerDashbaordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        loadDashboardData()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadDashboardData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = currentUser.uid

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                var totalSpent = 0.0
                var activeLaybys = 0
                val categoryData = mutableMapOf<String, Float>()

                documents.forEach { document ->
                    val amount = when (val value = document.get("totalAmount")) {
                        is Number -> value.toDouble()
                        else -> 0.0
                    }
                    totalSpent += amount

                    val category = document.getString("category") ?: "Other"
                    categoryData[category] = (categoryData[category] ?: 0f) + amount.toFloat()

                    val status = document.getString("status")?.lowercase() ?: ""
                    if (status.contains("layby") || status.contains("lay-by")) {
                        activeLaybys++
                    }
                }

                binding.tvTotalSpent.text = String.format("R%.2f", totalSpent)
                binding.tvActiveLaybys.text = activeLaybys.toString()

                if (categoryData.isNotEmpty()) {
                    binding.pieChart.setData(categoryData)
                    updateCategoryLegend(categoryData)
                } else {
                    binding.pieChart.setData(emptyMap())
                    binding.tvCategoryLegend.text = "No purchases yet"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to load dashboard data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCategoryLegend(categoryData: Map<String, Float>) {
        val total = categoryData.values.sum()
        if (total <= 0f) {
            binding.tvCategoryLegend.text = "No purchase data yet"
            return
        }

        val legend = categoryData.entries
            .sortedByDescending { it.value }
            .joinToString(" | ") { entry ->
                val percentage = (entry.value / total) * 100
                "${entry.key} (${String.format("%.0f", percentage)}%)"
            }
        binding.tvCategoryLegend.text = legend
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
}
