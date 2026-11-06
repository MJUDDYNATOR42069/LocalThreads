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

            Toast.makeText(
                this,
                "Edit Profile clicked",
                Toast.LENGTH_SHORT
            ).show()


        }


        binding.btnMyDashboard.setOnClickListener {

            Toast.makeText(
                this,
                "My Dashboard clicked",
                Toast.LENGTH_SHORT
            ).show()


        }


        binding.btnMyOrders.setOnClickListener {

            Toast.makeText(
                this,
                "My Orders clicked",
                Toast.LENGTH_SHORT
            ).show()


        }


        binding.btnMyLaybys.setOnClickListener {

            Toast.makeText(
                this,
                "My Lay-Bys clicked",
                Toast.LENGTH_SHORT
            ).show()


        }


        binding.btnLanguage.setOnClickListener {

            Toast.makeText(
                this,
                "Language settings clicked",
                Toast.LENGTH_SHORT
            ).show()


        }


        binding.btnNotifications.setOnClickListener {

            Toast.makeText(
                this,
                "Notifications clicked",
                Toast.LENGTH_SHORT
            ).show()


        }


        binding.btnPrivacy.setOnClickListener {

            Toast.makeText(
                this,
                "Privacy Policy clicked",
                Toast.LENGTH_SHORT
            ).show()



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

