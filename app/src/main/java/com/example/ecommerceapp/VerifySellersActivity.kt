package com.example.ecommerceapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.databinding.ActivityAdminRecordsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class VerifySellersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRecordsBinding
    private val db = FirebaseFirestore.getInstance()
    private val allRecords = mutableListOf<AdminRecord>()
    private lateinit var adapter: AdminRecordAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvScreenTitle.text = "Verify Sellers"
        binding.etSearch.hint = "Search seller applications..."

        adapter = AdminRecordAdapter(emptyList()) { record, action ->
            when (action) {
                1 -> approveSeller(record)
                2 -> rejectSeller(record)
            }
        }

        binding.rvRecords.layoutManager = LinearLayoutManager(this)
        binding.rvRecords.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecords(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadApplications()
    }

    private fun loadApplications() {
        binding.progressRecords.visibility = View.VISIBLE

        listener = db.collection("sellerApplications")
            .addSnapshotListener { snapshot, error ->

                binding.progressRecords.visibility = View.GONE

                if (error != null) {
                    showMessage("Could not load applications: ${error.message}")
                    return@addSnapshotListener
                }

                allRecords.clear()

                snapshot?.documents?.forEach { document ->

                    val storeName = document.getString("storename") ?: "Unnamed Store"
                    val email = document.getString("email") ?: "No email"
                    val description = document.getString("Description") ?: ""
                    val status = document.getString("status") ?: "pending"

                    allRecords.add(
                        AdminRecord(
                            documentId = document.id,
                            title = storeName,
                            subtitle = "$email\n$description",
                            status = status,
                            actionOne = if (status == "pending") "Approve" else "",
                            actionTwo = if (status == "pending") "Reject" else ""
                        )
                    )
                }

                filterRecords(binding.etSearch.text.toString())
            }
    }

    private fun filterRecords(query: String) {
        val filtered = allRecords.filter {
            it.title.contains(query, true) ||
                    it.subtitle.contains(query, true) ||
                    it.status.contains(query, true)
        }

        adapter.updateRecords(filtered)
        binding.tvRecordCount.text =
            "Applications (${filtered.size} of ${allRecords.size})"

        binding.rvRecords.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        binding.tvEmptyRecords.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun approveSeller(record: AdminRecord) {
        val adminUid = FirebaseAuth.getInstance().currentUser?.uid

        if (adminUid == null) {
            showMessage("Please sign in before approving sellers.")
            return
        }

        db.collection("sellerApplications")
            .document(record.documentId)
            .get()
            .addOnSuccessListener { application ->

                val sellerId = application.get("sellerId")?.toString()

                if (sellerId.isNullOrBlank()) {
                    showMessage("This application has no seller ID.")
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .whereEqualTo("userId", sellerId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { users ->

                        if (users.isEmpty) {
                            showMessage(
                                "No matching user was found for seller ID $sellerId. " +
                                        "Check the application UID before approving."
                            )
                            return@addOnSuccessListener
                        }

                        val batch = db.batch()
                        val userRef = users.documents.first().reference
                        val applicationRef = db.collection("sellerApplications")
                            .document(record.documentId)

                        batch.update(userRef, "role", "seller")
                        batch.update(applicationRef, mapOf(
                            "status" to "approved",
                            "reviewedAt" to FieldValue.serverTimestamp(),
                            "reviewedBy" to adminUid,
                            "rejectionReason" to ""
                        ))

                        batch.commit()
                            .addOnSuccessListener {
                                showMessage("Seller approved successfully.")
                            }
                            .addOnFailureListener {
                                showMessage("Approval failed: ${it.message}")
                            }
                    }
                    .addOnFailureListener {
                        showMessage("Could not find seller: ${it.message}")
                    }
            }
            .addOnFailureListener {
                showMessage("Could not retrieve application: ${it.message}")
            }
    }

    private fun rejectSeller(record: AdminRecord) {
        val input = EditText(this)
        input.hint = "Enter rejection reason"

        AlertDialog.Builder(this)
            .setTitle("Reject Seller Application")
            .setMessage("Provide a reason for rejecting ${record.title}.")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->

                val reason = input.text.toString().trim()

                if (reason.isBlank()) {
                    showMessage("Please enter a rejection reason.")
                    return@setPositiveButton
                }

                val adminUid = FirebaseAuth.getInstance().currentUser?.uid

                if (adminUid == null) {
                    showMessage("Please sign in first.")
                    return@setPositiveButton
                }

                db.collection("sellerApplications")
                    .document(record.documentId)
                    .update(mapOf(
                        "status" to "rejected",
                        "rejectionReason" to reason,
                        "reviewedAt" to FieldValue.serverTimestamp(),
                        "reviewedBy" to adminUid
                    ))
                    .addOnSuccessListener {
                        showMessage("Application rejected.")
                    }
                    .addOnFailureListener {
                        showMessage("Rejection failed: ${it.message}")
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }
}