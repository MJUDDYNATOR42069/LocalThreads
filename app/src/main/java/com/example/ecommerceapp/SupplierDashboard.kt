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
    private lateinit var localDb: LocalAppDatabase
    private var currentSupplierId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplier_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        localDb = LocalAppDatabase(this)
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
            val intent = Intent(this, ManageProductsActivity::class.java)
            startActivity(intent)
        }

        if (currentSupplierId != null) {
            loadSupplierDashboardData()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadSupplierDashboardData() {
        val uid = currentSupplierId ?: return

        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val supplierDocs = documents.filter { doc ->
                    val sId = doc.getString("sellerId") ?: doc.getString("SellerID") ?: doc.getString("supplierId") ?: ""
                    sId == uid || sId.isEmpty()
                }

                if (supplierDocs.isEmpty()) {
                    renderSupplierMetrics(0.0, 0, "No products listed yet", "Add products to see analytics", 0.0)
                    return@addOnSuccessListener
                }

                var totalRevenue = 0.0
                var totalUnitsSold = 0

                var topProductName = ""
                var topProductViews = 0
                var topProductSales = 0
                var topProductRevenue = 0.0
                var maxSales = -1

                for (document in supplierDocs) {
                    val name = document.getString("name") ?: document.getString("productName") ?: "Unknown Product"
                    val price = when (val p = document.get("price")) {
                        is Number -> p.toDouble()
                        is String -> p.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    val sales = when (val s = document.get("sales")) {
                        is Number -> s.toInt()
                        is String -> s.toIntOrNull() ?: 0
                        else -> 0
                    }
                    val views = when (val v = document.get("views")) {
                        is Number -> v.toInt()
                        is String -> v.toIntOrNull() ?: 0
                        else -> 0
                    }

                    val productRevenue = price * sales
                    totalRevenue += productRevenue
                    totalUnitsSold += sales

                    if (sales > maxSales) {
                        maxSales = sales
                        topProductName = name
                        topProductViews = views
                        topProductSales = sales
                        topProductRevenue = productRevenue
                    }
                }

                val topStatsStr = if (maxSales > 0) {
                    "Views: $topProductViews | Sold: $topProductSales"
                } else {
                    "Share your products to start selling"
                }

                localDb.saveSupplierMetrics(
                    uid,
                    totalRevenue,
                    totalUnitsSold,
                    if (maxSales > 0) topProductName else "No sales recorded yet",
                    topStatsStr,
                    topProductRevenue
                )

                renderSupplierMetrics(
                    totalRevenue,
                    totalUnitsSold,
                    if (maxSales > 0) topProductName else "No sales recorded yet",
                    topStatsStr,
                    topProductRevenue
                )
            }
            .addOnFailureListener { exception ->
                Log.e("SupplierDashboard", "Error getting products", exception)
                loadFromLocalDb(uid)
            }
    }

    private fun loadFromLocalDb(supplierId: String) {
        val cached = localDb.getSupplierMetrics(supplierId)
        if (cached != null) {
            renderSupplierMetrics(
                cached.revenue,
                cached.unitsSold,
                cached.topProductName,
                cached.topProductStats,
                cached.topProductRevenue
            )
        } else {
            renderSupplierMetrics(0.0, 0, "Dashboard Ready", "No local analytics cached", 0.0)
        }
    }

    private fun renderSupplierMetrics(
        revenue: Double,
        unitsSold: Int,
        topName: String,
        topStats: String,
        topRev: Double
    ) {
        Revenue.text = String.format("R%.2f", revenue)
        UnitsSold.text = unitsSold.toString()
        TopProductName.text = topName
        TopProductStats.text = topStats
        TopProductRevenue.text = String.format("R%.2f", topRev)
    }
}
