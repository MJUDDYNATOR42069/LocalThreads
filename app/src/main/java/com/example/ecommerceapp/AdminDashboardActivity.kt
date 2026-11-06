package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommerceapp.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnManageUsers.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        binding.btnVerifySellers.setOnClickListener {
            startActivity(Intent(this, VerifySellersActivity::class.java))
        }

        binding.btnManageProducts.setOnClickListener {
            startActivity(Intent(this, ManageProductsActivity::class.java))
        }

        binding.btnDisputes.setOnClickListener {
            startActivity(Intent(this, DisputesActivity::class.java))
        }
    }
}