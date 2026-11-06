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

                routeUser(user.uid, false)

            } catch (e: Exception) {
                Log.e("Login", "Email login failed", e)
                showMessage("Login failed: ${e.localizedMessage}")

            } finally {
                setLoading(false)
            }
        }
    }

    private fun routeUser(uid: String, createCustomerIfMissing: Boolean) {

        lifecycleScope.launch {
            try {
                val userDocument = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (!userDocument.exists()) {

                    if (createCustomerIfMissing) {
                        val currentUser = auth.currentUser

                        val profile = hashMapOf(
                            "userId" to uid,
                            "email" to (currentUser?.email ?: ""),
                            "firstName" to (currentUser?.displayName ?: ""),
                            "role" to "customer"
                        )

                        db.collection("users")
                            .document(uid)
                            .set(profile)
                            .await()

                        openDashboard("customer")
                    } else {
                        showMessage(
                            "Your account profile was not found. Please contact the administrator."
                        )
                    }

                    return@launch
                }

                val role = userDocument.getString("role")
                    ?.trim()
                    ?.lowercase()

                if (role.isNullOrEmpty()) {
                    showMessage("Your account has no assigned role.")
                    return@launch
                }

                openDashboard(role)

            } catch (e: Exception) {
                Log.e("Login", "Could not retrieve user role", e)
                showMessage("Could not load your account: ${e.localizedMessage}")
            }
        }
    }

    private fun openDashboard(role: String) {

        val destination = when (role) {
            "admin" -> AdminDashboardActivity::class.java
            "supplier" -> SupplierDashboard::class.java
            "customer" -> HomeActivity::class.java
            else -> {
                showMessage("Unrecognised account role: $role")
                return
            }
        }

        val intent = Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                        routeUser(user.uid, true)
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