package com.example.ecommerceapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etSurname = findViewById<TextInputEditText>(R.id.etSurname)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etAddress = findViewById<TextInputEditText>(R.id.etAddress)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        // Load current data (Optional - you can fetch existing data first)
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                etName.setText(doc.getString("fullName"))
                etSurname.setText(doc.getString("surname"))
                etPhone.setText(doc.getString("phone"))
                etAddress.setText(doc.getString("address"))
            }
        }

        btnSave.setOnClickListener {
            if (userId != null) {
                val updates = mapOf(
                    "fullName" to etName.text.toString(),
                    "surname" to etSurname.text.toString(),
                    "phone" to etPhone.text.toString(),
                    "address" to etAddress.text.toString()
                )
                db.collection("users").document(userId).update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}