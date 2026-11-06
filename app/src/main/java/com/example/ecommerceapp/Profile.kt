package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommerceapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()


        loadUserDetails()


        setupButtons()
    }



    private fun loadUserDetails() {

        val currentUser = auth.currentUser

        if (currentUser != null) {


            binding.tvProfileEmail.text =
                currentUser.email ?: "No email available"

            binding.tvProfileName.text =
                currentUser.displayName ?: "User"

        } else {


            binding.tvProfileName.text = "Guest"
            binding.tvProfileEmail.text = "Not logged in"
        }
    }



    private fun setupButtons() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.btnMyDashboard.setOnClickListener {
            startActivity(Intent(this, CustomerDashboard::class.java))
        }

        binding.btnMyOrders.setOnClickListener {
            startActivity(Intent(this, Orders::class.java))
        }

        binding.btnMyLaybys.setOnClickListener {
            startActivity(Intent(this, Laybys::class.java))
        }

        binding.btnLanguage.setOnClickListener {
            Toast.makeText(this, "Language settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, Notifications::class.java))
        }

        binding.btnPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicy::class.java))
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }


    private fun logoutUser() {

        auth.signOut()

        Toast.makeText(
            this,
            "Logged out successfully",
            Toast.LENGTH_SHORT
        ).show()


        val intent = Intent(this, Login::class.java)


        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }
}

