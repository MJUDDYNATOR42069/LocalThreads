package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommerceapp.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRegisterButton()
        setupLoginButton()
        setupGoogleButton()
        setupGoogleSignIn()
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }
    }

    private fun setupLoginButton() {
        binding.Login.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupGoogleButton() {
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google registration will be added next", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun validateInputs(): Boolean {
        val firstName = binding.oFirstName.text.toString().trim()
        val lastName = binding.oLastName.text.toString().trim()
        val email = binding.oEmail.text.toString().trim()
        val password = binding.oPassword.text.toString()
        val confirmPassword = binding.oConfirmPassword.text.toString()

        binding.FirstName.error = null
        binding.LastName.error = null
        binding.Email.error = null
        binding.Password.error = null
        binding.ConfirmPassword.error = null

        if (firstName.isEmpty()) {
            binding.FirstName.error = "First name is required"
            return false
        }
        if (lastName.isEmpty()) {
            binding.LastName.error = "Last name is required"
            return false
        }
        if (email.isEmpty()) {
            binding.Email.error = "Email is required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.Email.error = "Invalid email format"
            return false
        }
        if (password.isEmpty()) {
            binding.Password.error = "Password is required"
            return false
        }
        if (password.length < 8) {
            binding.Password.error = "Password must be at least 8 characters long"
            return false
        }
        if (!password.any { it.isUpperCase() }) {
            binding.Password.error = "Password must contain at least one capital letter"
            return false
        }
        if (password.count { it.isDigit() } < 2) {
            binding.Password.error = "Password must contain at least 2 numbers"
            return false
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            binding.Password.error = "Password must contain at least 1 special character"
            return false
        }
        if (confirmPassword.isEmpty()) {
            binding.ConfirmPassword.error = "Please confirm your password"
            return false
        }
        if (password != confirmPassword) {
            binding.ConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }

    private fun registerUser() {
        val firstName = binding.oFirstName.text.toString().trim()
        val lastName = binding.oLastName.text.toString().trim()
        val email = binding.oEmail.text.toString().trim()
        val password = binding.oPassword.text.toString()

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    resetButton()
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val userId = user.uid
                val userData = hashMapOf(
                    "userId" to userId,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "role" to "customer"
                )

                db.collection("users")
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        resetButton()
                        Toast.makeText(this, "Account created, but user information could not be saved", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { exception ->
                resetButton()
                Toast.makeText(this, exception.message ?: "Registration failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun resetButton() {
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "Create Account"
    }
}
