@file:Suppress("DEPRECATION")

package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecommerceapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        setupGoogleSignIn()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.Email.text.toString().trim()
            val password = binding.Password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validateInputs(email, password)) {
                signInWithEmail(email, password)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

        binding.CreateAccount.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.ForgotPassword.setOnClickListener {
            // Since ForgotPassword activity is missing, we'll show a toast for now
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email required"
            return false
        } else {
            binding.tilEmail.error = null
        }

        if (password.length < 8) {
            binding.tilPassword.error = "Password must be at least 8 characters"
            return false
        }

        if (!password.any { it.isUpperCase() }) {
            binding.tilPassword.error = "Password must have at least 1 capital letter"
            return false
        }

        if (password.count { it.isDigit() } < 2) {
            binding.tilPassword.error = "Password must have at least 2 numbers"
            return false
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            binding.tilPassword.error = "Password must have at least 1 special character"
            return false
        }

        binding.tilPassword.error = null
        return true
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = if (binding.radioAdmin.isChecked) {
                        Intent(this, AdminDashboardActivity::class.java)
                    } else {
                        Intent(this, MainActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            // Handle Google Sign In result here if needed
            Toast.makeText(this, "Google Sign In result received", Toast.LENGTH_SHORT).show()
        }
    }
}