package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SupplierDashboard : AppCompatActivity() {


    private lateinit var Revenue: TextView
    private lateinit var UnitsSold: TextView
    private lateinit var TopProductName: TextView
    private lateinit var TopProductStats: TextView
    private lateinit var TopProductRevenue: TextView
    private lateinit var SeeAllProducts: TextView
    private lateinit var btnBack: ImageView


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentSupplierId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplier_dashboard)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentSupplierId = auth.currentUser?.uid


        Revenue = findViewById(R.id.Revenue)
        UnitsSold = findViewById(R.id.UnitsSold)
        TopProductName = findViewById(R.id.TopProductName)
        TopProductStats = findViewById(R.id.TopProductStats)
        TopProductRevenue = findViewById(R.id.TopProductRevenue)
        SeeAllProducts = findViewById(R.id.SeeAllProducts)
        btnBack = findViewById(R.id.btnBack)


        btnBack.setOnClickListener {
            finish()
        }


        SeeAllProducts.setOnClickListener {

            val intent = Intent(this, SupplierDashboard::class.java)
            startActivity(intent)
        }

        // Load Data
        if (currentSupplierId != null) {
            loadSupplierDashboardData()
        } else {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadSupplierDashboardData() {
        // We query the 'products' collection for items where 'sellerId' matches the logged-in user
        db.collection("products")
            .whereEqualTo("sellerId", currentSupplierId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Handle case where supplier has no products yet
                    Revenue.text = "R0"
                    UnitsSold.text = "0"
                    TopProductName.text = "No products listed yet"
                    TopProductStats.text = "Add products to see analytics"
                    TopProductRevenue.text = "R0"
                    return@addOnSuccessListener
                }

                var totalRevenue = 0.0
                var totalUnitsSold = 0

                // Variables to track the top performing product
                var topProductName = ""
                var topProductViews = 0
                var topProductSales = 0
                var topProductRevenue = 0.0
                var maxSales = -1

                // Loop through each product document
                for (document in documents) {
                    val name = document.getString("name") ?: "Unknown Product"
                    val price = document.getDouble("price") ?: 0.0
                    val sales = document.getLong("sales")?.toInt() ?: 0
                    val views = document.getLong("views")?.toInt() ?: 0

                    // 1. Calculate Total Revenue: (Price * Units Sold)
                    val productRevenue = price * sales
                    totalRevenue += productRevenue

                    // 2. Calculate Total Units Sold
                    totalUnitsSold += sales

                    // 3. Determine Top Performing Product (based on most sales)
                    if (sales > maxSales) {
                        maxSales = sales
                        topProductName = name
                        topProductViews = views
                        topProductSales = sales
                        topProductRevenue = productRevenue
                    }
                }

                // Format and display the calculated totals
                Revenue.text = "R${String.format("%.2f", totalRevenue)}"
                UnitsSold.text = totalUnitsSold.toString()

                // Display Top Product Details
                if (maxSales > 0) {
                    TopProductName.text = topProductName
                    TopProductStats.text = "Views: $topProductViews | Sold: $topProductSales"
                    TopProductRevenue.text = "R${String.format("%.2f", topProductRevenue)}"
                } else {
                    TopProductName.text = "No sales yet"
                    TopProductStats.text = "Share your products to get sales"
                    TopProductRevenue.text = "R0"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SupplierDashboard", "Error getting documents: ", exception)
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }
}