package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ecommerceapp.databinding.ActivityCartBinding

class Cart : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding


    private var quantity1 = 1
    private var quantity2 = 1


    private val productPrice1 = 450.0
    private val productPrice2 = 280.0


    private val taxRate = 0.02
    private val deliveryFee = 10.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        setupQuantityButtons()
        calculateCart()
    }

    private fun setupButtons() {


        binding.btnBack.setOnClickListener {
            finish()
        }


        binding.btnDeleteAll.setOnClickListener {

            quantity1 = 0
            quantity2 = 0

            binding.tvQuantity1.text = "0"
            binding.tvQuantity2.text = "0"

            calculateCart()

            Toast.makeText(
                this,
                "All items removed",
                Toast.LENGTH_SHORT
            ).show()
        }


        binding.btnDeleteItem1.setOnClickListener {

            quantity1 = 0
            binding.tvQuantity1.text = "0"

            calculateCart()

            Toast.makeText(
                this,
                "Item removed",
                Toast.LENGTH_SHORT
            ).show()
        }


        binding.btnDeleteItem2.setOnClickListener {

            quantity2 = 0
            binding.tvQuantity2.text = "0"

            calculateCart()

            Toast.makeText(
                this,
                "Item removed",
                Toast.LENGTH_SHORT
            ).show()
        }


        binding.btnCheckout.setOnClickListener {
            val intent = Intent(this, Checkout::class.java)
            startActivity(intent)
        }


        binding.btnAddToWishlist.setOnClickListener {

            Toast.makeText(
                this,
                "Items added to wishlist",
                Toast.LENGTH_SHORT
            ).show()


        }
    }


    private fun setupQuantityButtons() {


        binding.btnMinus1.setOnClickListener {

            if (quantity1 > 0) {
                quantity1--
                binding.tvQuantity1.text = quantity1.toString()
                calculateCart()
            }
        }


        binding.btnPlus1.setOnClickListener {

            quantity1++
            binding.tvQuantity1.text = quantity1.toString()
            calculateCart()
        }


        binding.btnMinus2.setOnClickListener {

            if (quantity2 > 0) {
                quantity2--
                binding.tvQuantity2.text = quantity2.toString()
                calculateCart()
            }
        }


        binding.btnPlus2.setOnClickListener {

            quantity2++
            binding.tvQuantity2.text = quantity2.toString()
            calculateCart()
        }
    }


    private fun calculateCart() {


        val subtotal =
            (productPrice1 * quantity1) +
                    (productPrice2 * quantity2)


        val tax = subtotal * taxRate


        val total = subtotal + tax + deliveryFee


        binding.tvSubtotal.text =
            "R${String.format("%.2f", subtotal)}"


        if (subtotal <= 0) {

            binding.btnCheckout.isEnabled = false
            binding.btnAddToWishlist.isEnabled = false

        } else {

            binding.btnCheckout.isEnabled = true
            binding.btnAddToWishlist.isEnabled = true
        }
    }
}

