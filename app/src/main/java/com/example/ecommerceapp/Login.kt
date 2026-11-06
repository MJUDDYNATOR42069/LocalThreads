package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.ecommerceapp.databinding.ActivityLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        setupListeners()
    }

    private fun setupListeners() {

        binding.btnLogin.setOnClickListener {
            loginWithEmail()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.CreateAccount.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.ForgotPassword.setOnClickListener {
            sendPasswordReset()
        }
    }

    private fun loginWithEmail() {

        val email = binding.Email.text.toString().trim()
        val password = binding.Password.text.toString()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }

        lifecycleScope.launch {
            setLoading(true)

            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user == null) {
                    showMessage("Login failed. Please try again.")
                    return@launch
                }

                routeUser(user.uid)

            } catch (e: Exception) {
                Log.e("Login", "Email login failed", e)
                showMessage("Login failed: ${e.localizedMessage}")

            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun routeUser(uid: String) {
        val currentUser = auth.currentUser
        val email = currentUser?.email?.lowercase() ?: ""

        val inferredRole = when {
            email.contains("admin") -> "admin"
            email.contains("supplier") || email.contains("seller") -> "supplier"
            else -> "customer"
        }

        var role = inferredRole

        try {
            val userDocument = db.collection("users")
                .document(uid)
                .get()
                .await()

            if (userDocument.exists()) {
                val fetchedRole = userDocument.getString("role")?.trim()?.lowercase()
                if (!fetchedRole.isNullOrEmpty()) {
                    role = fetchedRole
                }
            } else {
                try {
                    val profile = hashMapOf(
                        "userId" to uid,
                        "email" to (currentUser?.email ?: ""),
                        "firstName" to (currentUser?.displayName ?: ""),
                        "role" to inferredRole
                    )
                    db.collection("users").document(uid).set(profile).await()
                } catch (e: Exception) {
                    Log.w("Login", "Could not save user profile to Firestore", e)
                }
            }
        } catch (e: Exception) {
            Log.w("Login", "Could not read Firestore profile, using inferred role: $role", e)
        }

        openDashboard(role)
    }

    private fun openDashboard(role: String) {
        val destination = when (role.trim().lowercase()) {
            "admin" -> AdminDashboardActivity::class.java
            "supplier", "seller" -> SupplierDashboard::class.java
            else -> MainActivity::class.java
        }

        val intent = Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private fun signInWithGoogle() {

        lifecycleScope.launch {
            setLoading(true)

            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        getString(R.string.default_web_client_id)
                    )
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = this@Login,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleCredential.idToken,
                        null
                    )

                    val authResult =
                        auth.signInWithCredential(firebaseCredential).await()

                    val user = authResult.user

                    if (user != null) {
                        routeUser(user.uid)
                    } else {
                        showMessage("Google login failed.")
                    }

                } else {
                    showMessage("Unexpected Google sign-in credential.")
                }

            } catch (e: GetCredentialException) {
                Log.e("Login", "Google credential error", e)
                showMessage("Google sign-in failed: ${e.localizedMessage}")

            } catch (e: Exception) {
                Log.e("Login", "Google authentication error", e)
                showMessage("Google login failed: ${e.localizedMessage}")

            } finally {
                setLoading(false)
            }
        }
    }

    private fun sendPasswordReset() {

        val email = binding.Email.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Enter your email address first"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                showMessage("Password reset email sent.")
            }
            .addOnFailureListener { e ->
                showMessage("Could not send reset email: ${e.localizedMessage}")
            }
    }

    private fun setLoading(loading: Boolean) {
        if (isFinishing || isDestroyed) return

        binding.btnLogin.isEnabled = !loading
        binding.btnGoogleSignIn.isEnabled = !loading

        binding.btnLogin.text = if (loading) {
            "Please wait..."
        } else {
            "Login"
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
