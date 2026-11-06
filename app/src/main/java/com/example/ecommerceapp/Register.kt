package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.ecommerceapp.databinding.ActivityRegisterBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRegisterButton()
        setupLoginButton()
        setupGoogleButton()
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
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun setupGoogleButton() {
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
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

        val role = when {
            email.lowercase().contains("admin") -> "admin"
            email.lowercase().contains("supplier") || email.lowercase().contains("seller") -> "supplier"
            else -> "customer"
        }

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    resetButton()
                    val msg = task.exception?.message ?: "Registration failed"
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val user = task.result?.user
                if (user == null) {
                    resetButton()
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                saveUserProfile(user, firstName, lastName, email, role)
            }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(this@Register, request)
                handleGoogleCredential(result.credential)
            } catch (e: GetCredentialException) {
                Log.e("Register", "Credential error: ${e.message}")
                Toast.makeText(this@Register, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleCredential(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (!task.isSuccessful) {
                            resetButton()
                            Toast.makeText(
                                this,
                                "Google auth failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            return@addOnCompleteListener
                        }

                        val user = task.result?.user ?: return@addOnCompleteListener

                        val displayName = user.displayName ?: ""
                        val parts = displayName.split(" ", limit = 2)
                        val firstName = parts.getOrNull(0) ?: ""
                        val lastName = parts.getOrNull(1) ?: ""
                        val email = user.email ?: ""

                        val role = when {
                            email.lowercase().contains("admin") -> "admin"
                            email.lowercase().contains("supplier") || email.lowercase().contains("seller") -> "supplier"
                            else -> "customer"
                        }

                        saveUserProfile(user, firstName, lastName, email, role)
                    }
            } catch (e: Exception) {
                Log.e("Register", "Error parsing Google credential: ${e.message}")
                Toast.makeText(this, "Google sign-in parsing failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Unexpected credential type", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserProfile(
        user: FirebaseUser,
        firstName: String,
        lastName: String,
        email: String,
        role: String
    ) {
        val userId = user.uid
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val existingRole = snapshot.getString("role") ?: role
                    val updates = mutableMapOf<String, Any>()
                    if (snapshot.getString("firstName").isNullOrEmpty() && firstName.isNotEmpty()) {
                        updates["firstName"] = firstName
                    }
                    if (snapshot.getString("lastName").isNullOrEmpty() && lastName.isNotEmpty()) {
                        updates["lastName"] = lastName
                    }
                    if (snapshot.getString("email").isNullOrEmpty() && email.isNotEmpty()) {
                        updates["email"] = email
                    }
                    if (snapshot.getString("role").isNullOrEmpty()) {
                        updates["role"] = role
                    }
                    if (snapshot.getString("userId").isNullOrEmpty()) {
                        updates["userId"] = userId
                    }

                    if (updates.isEmpty()) {
                        navigateToDashboard(existingRole)
                    } else {
                        userDocRef.update(updates)
                            .addOnSuccessListener { navigateToDashboard(existingRole) }
                            .addOnFailureListener {
                                navigateToDashboard(existingRole)
                            }
                    }
                } else {
                    val userData = hashMapOf(
                        "userId" to userId,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "role" to role
                    )
                    userDocRef.set(userData)
                        .addOnSuccessListener { navigateToDashboard(role) }
                        .addOnFailureListener {
                            navigateToDashboard(role)
                        }
                }
            }
            .addOnFailureListener {
                navigateToDashboard(role)
            }
    }

    private fun navigateToDashboard(role: String) {
        Toast.makeText(this, "Account ready!", Toast.LENGTH_SHORT).show()
        val destination = when (role.trim().lowercase()) {
            "admin" -> AdminDashboardActivity::class.java
            "supplier", "seller" -> SupplierDashboard::class.java
            else -> MainActivity::class.java
        }
        val intent = Intent(this, destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun resetButton() {
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "Create Account"
    }
}
