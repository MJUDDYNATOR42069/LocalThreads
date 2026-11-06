package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class Checkout : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var subtotal = 0.0
    private var tax = 0.0
    private var total = 0.0
    private var isSavingOrder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        subtotal = intent.getDoubleExtra(EXTRA_SUBTOTAL, 0.0)
        tax = intent.getDoubleExtra(EXTRA_TAX, 0.0)
        total = intent.getDoubleExtra(EXTRA_TOTAL, subtotal + DELIVERY_FEE)
        findViewById<android.widget.TextView>(R.id.tvSubtotal).text = money(subtotal)
        findViewById<android.widget.TextView>(R.id.tvTax).text = money(tax)
        findViewById<android.widget.TextView>(R.id.tvDeliveryFee).text = money(DELIVERY_FEE)
        findViewById<android.widget.TextView>(R.id.tvTotal).text = money(total)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.btnPlaceOrder).setOnClickListener { confirmPayment() }
        findViewById<android.widget.RadioGroup>(R.id.radioGroupPayment).setOnCheckedChangeListener { _, checkedId ->
            findViewById<android.view.View>(R.id.llLayByDetails).visibility =
                if (checkedId == R.id.radioLayBy) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun confirmPayment() {
        val layBy = findViewById<android.widget.RadioGroup>(R.id.radioGroupPayment).checkedRadioButtonId == R.id.radioLayBy
        AlertDialog.Builder(this)
            .setTitle(if (layBy) "Confirm first installment" else "Confirm payment")
            .setMessage(
                if (layBy) "Continue only after you have paid the first lay-by installment."
                else "Continue only after your card or EFT payment has succeeded."
            )
            .setNegativeButton("Not yet", null)
            .setPositiveButton("Payment complete") { _, _ -> saveOrder(layBy) }
            .show()
    }

    private fun saveOrder(layBy: Boolean) {
        if (isSavingOrder) return
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please sign in before placing an order", Toast.LENGTH_LONG).show()
            return
        }
        if (total <= 0.0) {
            Toast.makeText(this, "Your order total is invalid", Toast.LENGTH_SHORT).show()
            return
        }

        val reference = db.collection("orders").document()
        val orderId = "ORD-${reference.id.takeLast(8).uppercase(Locale.ROOT)}"
        val items = intent.getStringArrayListExtra(EXTRA_ITEMS).orEmpty()
        val order = hashMapOf<String, Any>(
            "orderId" to orderId,
            "userId" to user.uid,
            "totalAmount" to total,
            "subtotal" to subtotal,
            "tax" to tax,
            "status" to if (layBy) "Lay-by" else "Paid",
            "paymentMethod" to if (layBy) "Lay-by (3 monthly installments)" else "Full payment",
            "items" to items,
            "createdAt" to FieldValue.serverTimestamp(),
            "paidAt" to FieldValue.serverTimestamp()
        )

        isSavingOrder = true
        findViewById<android.view.View>(R.id.btnPlaceOrder).isEnabled = false
        reference.set(order)
            .addOnSuccessListener {
                Toast.makeText(this, "Order $orderId saved", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, Orders::class.java))
                finish()
            }
            .addOnFailureListener { error ->
                isSavingOrder = false
                findViewById<android.view.View>(R.id.btnPlaceOrder).isEnabled = true
                Toast.makeText(this, "Could not save order: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun money(amount: Double) = String.format(Locale.US, "R%.2f", amount)

    companion object {
        const val EXTRA_SUBTOTAL = "checkout_subtotal"
        const val EXTRA_TAX = "checkout_tax"
        const val EXTRA_TOTAL = "checkout_total"
        const val EXTRA_ITEMS = "checkout_items"
        private const val DELIVERY_FEE = 10.0
    }
}
