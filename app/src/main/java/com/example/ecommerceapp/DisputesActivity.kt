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

class DisputesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRecordsBinding
    private lateinit var localDb: LocalAppDatabase
    private val db = FirebaseFirestore.getInstance()
    private val allRecords = mutableListOf<AdminRecord>()
    private lateinit var adapter: AdminRecordAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        localDb = LocalAppDatabase(this)

        binding.tvScreenTitle.text = "Manage Disputes"
        binding.etSearch.hint = "Search disputes..."

        adapter = AdminRecordAdapter(emptyList()) { record, action ->
            when (action) {
                1 -> updateDispute(record, "in_review")
                2 -> resolveDispute(record)
                3 -> rejectDispute(record)
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

        loadDisputes()
    }

    private fun loadDisputes() {
        binding.progressRecords.visibility = View.VISIBLE

        listener = db.collection("disputes")
            .addSnapshotListener { snapshot, error ->

                binding.progressRecords.visibility = View.GONE

                if (error != null || snapshot == null) {
                    loadFromLocalDb()
                    return@addSnapshotListener
                }

                allRecords.clear()

                snapshot.documents.forEach { document ->

                    val subject = document.getString("subject") ?: "Untitled dispute"
                    val description = document.getString("description") ?: ""
                    val buyerId = document.getString("buyerId") ?: "Unknown buyer"
                    val sellerId = document.getString("sellerId") ?: "Unknown seller"
                    val status = document.getString("status") ?: "open"

                    allRecords.add(
                        AdminRecord(
                            documentId = document.id,
                            title = subject,
                            subtitle = "$description\nBuyer: $buyerId\nSeller: $sellerId",
                            status = status,
                            actionOne = if (status == "open") "Mark In Review" else "",
                            actionTwo = if (status == "open" || status == "in_review") "Resolve" else "",
                            actionThree = if (status == "open" || status == "in_review") "Reject" else ""
                        )
                    )
                }

                localDb.saveAdminRecords("dispute", allRecords)
                filterRecords(binding.etSearch.text.toString())
            }
    }

    private fun loadFromLocalDb() {
        val cached = localDb.getAdminRecords("dispute")
        allRecords.clear()
        allRecords.addAll(cached)
        filterRecords(binding.etSearch.text.toString())
    }

    private fun filterRecords(query: String) {
        val filtered = allRecords.filter {
            it.title.contains(query, true) ||
                    it.subtitle.contains(query, true) ||
                    it.status.contains(query, true)
        }

        adapter.updateRecords(filtered)

        binding.tvRecordCount.text =
            "Disputes (${filtered.size} of ${allRecords.size})"

        binding.rvRecords.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        binding.tvEmptyRecords.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateDispute(record: AdminRecord, newStatus: String) {
        db.collection("disputes")
            .document(record.documentId)
            .update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener {
                showMessage("Dispute moved to review.")
            }
            .addOnFailureListener {
                showMessage("Update failed: ${it.message}")
            }
    }

    private fun resolveDispute(record: AdminRecord) {
        showResolutionDialog(record, "resolved")
    }

    private fun rejectDispute(record: AdminRecord) {
        showResolutionDialog(record, "rejected")
    }

    private fun showResolutionDialog(record: AdminRecord, newStatus: String) {
        val input = EditText(this)
        input.hint = "Enter resolution details"

        AlertDialog.Builder(this)
            .setTitle(if (newStatus == "resolved") "Resolve Dispute" else "Reject Dispute")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->

                val resolution = input.text.toString().trim()

                if (resolution.isBlank()) {
                    showMessage("Please enter resolution details.")
                    return@setPositiveButton
                }

                val adminUid = FirebaseAuth.getInstance().currentUser?.uid

                if (adminUid == null) {
                    showMessage("Please sign in first.")
                    return@setPositiveButton
                }

                db.collection("disputes")
                    .document(record.documentId)
                    .update(
                        mapOf(
                            "status" to newStatus,
                            "resolution" to resolution,
                            "resolvedBy" to adminUid,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener {
                        showMessage("Dispute $newStatus successfully.")
                    }
                    .addOnFailureListener {
                        showMessage("Could not update dispute: ${it.message}")
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
