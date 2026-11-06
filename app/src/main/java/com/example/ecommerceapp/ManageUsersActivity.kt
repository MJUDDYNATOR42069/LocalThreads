package com.example.ecommerceapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.databinding.ActivityManageUsersBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUsersBinding
    private lateinit var localDb: LocalAppDatabase
    private val db = FirebaseFirestore.getInstance()

    private lateinit var userAdapter: UserAdapter
    private val allUsers = mutableListOf<UserAccount>()

    private var usersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        localDb = LocalAppDatabase(this)

        setupRecyclerView()
        setupSearch()
        setupButtons()
        loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList()) { user ->
            showAccountConfirmation(user)
        }

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(this@ManageUsersActivity)
            adapter = userAdapter
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUsers() {
        binding.progressUsers.visibility = View.VISIBLE
        binding.tvEmptyUsers.visibility = View.GONE

        usersListener = db.collection("users")
            .addSnapshotListener { snapshot, error ->

                binding.progressUsers.visibility = View.GONE

                if (error != null) {
                    loadFromLocalDb()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    loadFromLocalDb()
                    return@addSnapshotListener
                }

                allUsers.clear()
                val adminRecords = mutableListOf<AdminRecord>()

                for (document in snapshot.documents) {
                    val user = UserAccount(
                        documentId = document.id,
                        userId = document.getString("userId") ?: document.id,
                        firstName = document.getString("firstName") ?: "",
                        lastName = document.getString("lastName") ?: "",
                        email = document.getString("email") ?: "",
                        role = document.getString("role") ?: "customer",
                        accountStatus = document.getBoolean("accountStatus") ?: true
                    )

                    allUsers.add(user)
                    adminRecords.add(
                        AdminRecord(
                            documentId = user.documentId,
                            title = "${user.firstName} ${user.lastName}".trim(),
                            subtitle = "${user.email}\nRole: ${user.role}",
                            status = if (user.accountStatus) "active" else "disabled"
                        )
                    )
                }

                localDb.saveAdminRecords("user", adminRecords)
                filterUsers(binding.etSearchUsers.text.toString())
            }
    }

    private fun loadFromLocalDb() {
        val cachedRecords = localDb.getAdminRecords("user")
        allUsers.clear()
        for (rec in cachedRecords) {
            val nameParts = rec.title.split(" ", limit = 2)
            allUsers.add(
                UserAccount(
                    documentId = rec.documentId,
                    userId = rec.documentId,
                    firstName = nameParts.getOrNull(0) ?: "",
                    lastName = nameParts.getOrNull(1) ?: "",
                    email = rec.subtitle.substringBefore("\n"),
                    role = rec.subtitle.substringAfter("Role: ", "customer"),
                    accountStatus = rec.status == "active"
                )
            )
        }
        filterUsers(binding.etSearchUsers.text.toString())
    }

    private fun setupSearch() {
        binding.etSearchUsers.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterUsers(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun filterUsers(searchText: String) {
        val query = searchText.trim().lowercase()

        val filteredUsers = allUsers.filter { user ->
            user.firstName.lowercase().contains(query) ||
                    user.lastName.lowercase().contains(query) ||
                    user.email.lowercase().contains(query) ||
                    user.role.lowercase().contains(query)
        }

        userAdapter.updateUsers(filteredUsers)

        binding.tvUserCount.text =
            "Users (${filteredUsers.size} of ${allUsers.size})"

        updateEmptyState(filteredUsers.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean = allUsers.isEmpty()) {
        binding.tvEmptyUsers.visibility =
            if (isEmpty) View.VISIBLE else View.GONE

        binding.rvUsers.visibility =
            if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showAccountConfirmation(user: UserAccount) {
        val newStatus = !user.accountStatus

        val action = if (newStatus) "activate" else "deactivate"

        AlertDialog.Builder(this)
            .setTitle("Confirm Account Change")
            .setMessage(
                "Are you sure you want to $action " +
                        "${user.firstName} ${user.lastName}'s account?"
            )
            .setPositiveButton("Confirm") { _, _ ->
                updateAccountStatus(user, newStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateAccountStatus(
        user: UserAccount,
        newStatus: Boolean
    ) {
        db.collection("users")
            .document(user.documentId)
            .update("accountStatus", newStatus)
            .addOnSuccessListener {
                val message = if (newStatus) {
                    "Account activated successfully."
                } else {
                    "Account deactivated successfully."
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Failed to update account: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroy() {
        usersListener?.remove()
        super.onDestroy()
    }
}
