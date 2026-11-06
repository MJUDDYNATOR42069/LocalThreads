package com.example.ecommerceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.text.DateFormat

data class OrderRecord(
    val id: String,
    val total: Double,
    val status: String,
    val paymentMethod: String,
    val createdAt: Timestamp?
)

class Orders : AppCompatActivity() {
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var emptyMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        emptyMessage = findViewById(R.id.tvEmptyOrders)
        ordersAdapter = OrdersAdapter()
        findViewById<RecyclerView>(R.id.recyclerOrders).apply {
            layoutManager = LinearLayoutManager(this@Orders)
            adapter = ordersAdapter
        }
        loadOrders()
    }

    private fun loadOrders() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            emptyMessage.text = "Sign in to view your order history."
            emptyMessage.visibility = View.VISIBLE
            return
        }

        FirebaseFirestore.getInstance().collection("orders")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { result ->
                val orders = result.documents.map { document ->
                    OrderRecord(
                        id = document.getString("orderId") ?: document.id,
                        total = (document.get("totalAmount") as? Number)?.toDouble() ?: 0.0,
                        status = document.getString("status") ?: "Paid",
                        paymentMethod = document.getString("paymentMethod") ?: "",
                        createdAt = document.getTimestamp("createdAt")
                    )
                }.sortedByDescending { it.createdAt?.seconds ?: 0L }
                ordersAdapter.submit(orders)
                emptyMessage.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                emptyMessage.text = "Could not load your orders. Please try again."
                emptyMessage.visibility = View.VISIBLE
                Toast.makeText(this, "Unable to load order history", Toast.LENGTH_SHORT).show()
            }
    }
}

private class OrdersAdapter : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {
    private var orders: List<OrderRecord> = emptyList()

    fun submit(items: List<OrderRecord>) {
        orders = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) =
        holder.bind(orders[position])

    override fun getItemCount(): Int = orders.size

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val orderId: TextView = view.findViewById(R.id.tvOrderId)
        private val total: TextView = view.findViewById(R.id.tvOrderTotal)
        private val status: TextView = view.findViewById(R.id.tvOrderStatus)
        private val date: TextView = view.findViewById(R.id.tvOrderDate)
        private val payment: TextView = view.findViewById(R.id.tvOrderPayment)

        fun bind(order: OrderRecord) {
            orderId.text = "Order #${order.id}"
            total.text = String.format("R%.2f", order.total)
            status.text = "Status: ${order.status}"
            payment.text = order.paymentMethod
            payment.visibility = if (order.paymentMethod.isBlank()) View.GONE else View.VISIBLE
            date.text = order.createdAt?.toDate()?.let {
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it)
            } ?: "Just placed"
        }
    }
}
