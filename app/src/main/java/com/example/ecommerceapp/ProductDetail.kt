package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var vpProductImages: ViewPager2
    private lateinit var llIndicators: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)



        vpProductImages = findViewById(R.id.vpProductImages)
        llIndicators = findViewById(R.id.llIndicators)



        val productImages = listOf(
            "https://firebasestorage.googleapis.com/.../image1.jpg",
            "https://firebasestorage.googleapis.com/.../image2.jpg",
            "https://firebasestorage.googleapis.com/.../image3.jpg"
        )



        val imageAdapter = ProductImageAdapter(productImages)

        vpProductImages.adapter = imageAdapter



        val dots = arrayOfNulls<ImageView>(productImages.size)

        for (i in productImages.indices) {

            dots[i] = ImageView(this)

            val widthHeight = 16

            val params = LinearLayout.LayoutParams(
                widthHeight,
                widthHeight
            )

            params.setMargins(8, 0, 8, 0)

            dots[i]?.layoutParams = params

            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.circle_white_border
                )
            )

            llIndicators.addView(dots[i])
        }



        if (dots.isNotEmpty()) {

            dots[0]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.circle_purple
                )
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
                                if (i == position) {
                                    R.drawable.circle_purple
                                } else {
                                    R.drawable.circle_white_border
                                }
                            )
                        )
                    }
                }
            }
        )

        findViewById<View>(R.id.btnAddToCart)?.setOnClickListener {
            val intent = Intent(this, Cart::class.java)
            startActivity(intent)
        }
    }
}