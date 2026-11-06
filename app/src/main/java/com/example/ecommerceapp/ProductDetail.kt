package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var vpProductImages: ViewPager2
    private lateinit var llIndicators: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val productName = intent.getStringExtra("product_name") ?: "Stylish Product"
        val productPrice = intent.getDoubleExtra("product_price", 0.0)
        val productImage = intent.getStringExtra("product_image") ?: "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=600"

        findViewById<TextView>(R.id.tvProductName)?.text = productName
        val priceText = if (productPrice > 0.0) "R${String.format("%.2f", productPrice)}" else "R450.00"
        findViewById<TextView>(R.id.tvProductPrice)?.text = priceText

        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        vpProductImages = findViewById(R.id.vpProductImages)
        llIndicators = findViewById(R.id.llIndicators)

        val productImages = listOf(productImage)
        val imageAdapter = ProductImageAdapter(productImages)
        vpProductImages.adapter = imageAdapter

        val dots = arrayOfNulls<ImageView>(productImages.size)
        for (i in productImages.indices) {
            dots[i] = ImageView(this)
            val widthHeight = 16
            val params = LinearLayout.LayoutParams(widthHeight, widthHeight)
            params.setMargins(8, 0, 8, 0)
            dots[i]?.layoutParams = params
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.circle_white_border)
            )
            llIndicators.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[0]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.circle_purple)
            )
        }

        vpProductImages.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    for (i in productImages.indices) {
                        dots[i]?.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@ProductDetailActivity,
                                if (i == position) R.drawable.circle_purple else R.drawable.circle_white_border
                            )
                        )
                    }
                }
            }
        )

        val rvAlsoBought = findViewById<RecyclerView>(R.id.rvAlsoBought)
        if (rvAlsoBought != null) {
            rvAlsoBought.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvAlsoBought.adapter = ProductAdapter(ProductRepository.recommended.take(4))
        }

        findViewById<View>(R.id.btnAddToCart)?.setOnClickListener {
            val intent = Intent(this, Cart::class.java)
            startActivity(intent)
        }
    }
}
