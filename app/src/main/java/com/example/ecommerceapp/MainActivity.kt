package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Load the Home Fragment by default when MainActivity opens
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        // Handle bottom navigation item clicks
        bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()

                // If Search is an Activity, use startActivity.
                // If it's a Fragment, use selectedFragment = SearchFragment()
                R.id.nav_search -> {
                    startActivity(Intent(this, Search::class.java))
                    return@setOnItemSelectedListener false // Keep Home highlighted
                }

                // Same for Cart
                R.id.nav_cart -> {
                    startActivity(Intent(this, Cart::class.java))
                    return@setOnItemSelectedListener false
                }

                R.id.nav_orders -> {
                    startActivity(Intent(this, Orders::class.java))
                    return@setOnItemSelectedListener false
                }

                // Wishlist Fragment
                R.id.nav_wishlist -> selectedFragment = WishlistFragment()

                // Profile Fragment
                R.id.nav_profile -> selectedFragment = ProfileFragment()
            }

            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commit()
            }
            true
        }
    }
}
