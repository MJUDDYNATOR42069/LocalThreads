package com.example.ecommerceapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.databinding.ActivityAdminRecordsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ManageProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRecordsBinding
    private val db = FirebaseFirestore.getInstance()
    private val allRecords = mutableListOf<AdminRecord>()
    private lateinit var adapter: AdminRecordAdapter
    private var listener: ListenerRegistration? = null

    private val productStatuses = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvScreenTitle.text = "Manage Products"
        binding.etSearch.hint = "Search products..."

        adapter = AdminRecordAdapter(emptyList()) { record, action ->
            if (action == 1) {
                toggleProductStatus(record)
            }
        }

        binding.rvRecords.layoutManager = LinearLayoutManager(this)
        binding.rvRecords.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecords(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadProducts()
    }

    private fun loadProducts() {
        binding.progressRecords.visibility = View.VISIBLE

        listener = db.collection("products")
            .addSnapshotListener { snapshot, error ->

                binding.progressRecords.visibility = View.GONE

                if (error != null) {
                    showMessage("Could not load products: ${error.message}")
                    return@addSnapshotListener
                }

                allRecords.clear()
                productStatuses.clear()

                snapshot?.documents?.forEach { document ->

                    val name = document.getString("productName") ?: "Unnamed Product"
                    val brand = document.getString("brand") ?: "Unknown brand"
                    val sellerId = document.get("SellerID")?.toString() ?: "Unknown seller"
                    val price = document.getDouble("price")?.toString() ?: "0.00"
                    val stock = document.get("stock")?.toString() ?: "0"
                    val status = document.getBoolean("status") ?: false

                    productStatuses[document.id] = status

                    allRecords.add(
                        AdminRecord(
                            documentId = document.id,
                            title = name,
                            subtitle = "Brand: $brand\nPrice: R$price\nStock: $stock\nSeller ID: $sellerId",
                            status = if (status) "Active" else "Inactive",
                            actionOne = if (status) "Deactivate Listing" else "Activate Listing"
                        )
                    )
                }

                filterRecords(binding.etSearch.text.toString())
            }
    }

    private fun filterRecords(query: String) {
        val filtered = allRecords.filter {
            it.title.contains(query, true) ||
                    it.subtitle.contains(query, true) ||
                    it.status.contains(query, true)
        }

        adapter.updateRecords(filtered)

        binding.tvRecordCount.text =
            "Products (${filtered.size} of ${allRecords.size})"

        binding.rvRecords.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        binding.tvEmptyRecords.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun toggleProductStatus(record: AdminRecord) {
        val currentStatus = productStatuses[record.documentId] ?: false
        val newStatus = !currentStatus

        val action = if (newStatus) "activate" else "deactivate"

        AlertDialog.Builder(this)
            .setTitle("Confirm Product Change")
            .setMessage("Are you sure you want to $action ${record.title}?")
            .setPositiveButton("Confirm") { _, _ ->

                db.collection("products")
                    .document(record.documentId)
                    .update("status", newStatus)
                    .addOnSuccessListener {
                        showMessage("Product listing updated.")
                    }
                    .addOnFailureListener {
                        showMessage("Update failed: ${it.message}")
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }
}