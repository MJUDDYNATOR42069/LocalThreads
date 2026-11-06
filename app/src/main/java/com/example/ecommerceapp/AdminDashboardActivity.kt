package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalRevenue: TextView
    private lateinit var btnAdminLogout: ImageView
    private lateinit var btnManageUsers: LinearLayout
    private lateinit var btnVerifySellers: LinearLayout
    private lateinit var btnManageProducts: LinearLayout
    private lateinit var btnResolveDisputes: LinearLayout

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalProducts = findViewById(R.id.tvTotalProducts)
        tvTotalOrders = findViewById(R.id.tvTotalOrders)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        btnAdminLogout = findViewById(R.id.btnAdminLogout)
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnVerifySellers = findViewById(R.id.btnVerifySellers)
        btnManageProducts = findViewById(R.id.btnManageProducts)
        btnResolveDisputes = findViewById(R.id.btnResolveDisputes)

        loadDashboardStats()

        btnAdminLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnManageUsers.setOnClickListener {
            Toast.makeText(this, "Opening User Management...", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        btnVerifySellers.setOnClickListener {
            Toast.makeText(this, "Opening Supplier Verification...", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, VerifySellersActivity::class.java))
        }

        btnManageProducts.setOnClickListener {
            Toast.makeText(this, "Opening Product Management...", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, ManageProductsActivity::class.java))
        }

        btnResolveDisputes.setOnClickListener {
            Toast.makeText(this, "Opening Dispute Resolution...", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, DisputesActivity::class.java))
        }
    }

    private fun loadDashboardStats() {
        // 1. Count Total Users
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                tvTotalUsers.text = snapshot.size().toString()
            }

        // 2. Count Total Products
        db.collection("products").get()
            .addOnSuccessListener { snapshot ->
                tvTotalProducts.text = snapshot.size().toString()
            }

        // 3. Count Total Orders AND Calculate Total Revenue
        db.collection("orders").get()
            .addOnSuccessListener { snapshot ->
                tvTotalOrders.text = snapshot.size().toString()

                var revenue = 0.0
                for (document in snapshot) {
                    val amount = document.getDouble("totalAmount") ?: 0.0
                    revenue += amount
                }
                tvTotalRevenue.text = "${String.format("%.2f", revenue)}"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading stats: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}