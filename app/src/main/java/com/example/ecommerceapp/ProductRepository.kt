package com.example.ecommerceapp

import com.example.ecommerceapp.model.Product

object ProductRepository {

    val recommended: List<Product> = listOf(
        Product("1", "Hoodie (h)", "New", "L", 499.0, "Black", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1556905055-8f358a7a47b2"),
        Product("2", "Dress (d)", "Dresses", "M", 599.0, "Red", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1595777457583-95e059d581b8")
    )

    val trending: List<Product> = listOf(
        Product("3", "Cap (c)", "Accessories", "Free", 199.0, "Blue", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1588850561407-ed78c282e89b"),
        Product("4", "Sneakers (s)", "Shoes", "8", 899.0, "White", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff")
    )

    val popular: List<Product> = listOf(
        Product("5", "Jacket (j)", "New", "XL", 799.0, "Brown", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1551028719-00167b16eac5"),
        Product("6", "Jeans (je)", "Second-hand", "32", 450.0, "Blue", "LocalThreads", "Second-hand",
            "https://images.unsplash.com/photo-1541099649105-f69ad21f3246")
    )

    val all: List<Product> = recommended + trending + popular

    fun byCategory(category: String): List<Product> = when (category) {
        ProductListActivity.CATEGORY_RECOMMENDED -> recommended
        ProductListActivity.CATEGORY_TRENDING    -> trending
        ProductListActivity.CATEGORY_POPULAR     -> popular
        ProductListActivity.CATEGORY_ALL         -> all
        else                                     -> emptyList()
    }

    /** Filters the full catalog by a chip label (New / Second-hand / T-Shirts / Dresses). */
    fun byChip(chip: String): List<Product> = when (chip) {
        "New"          -> all.filter { it.condition.equals("New", true) }
        "Second-hand"  -> all.filter { it.condition.equals("Second-hand", true) }
        "T-Shirts"     -> all.filter { it.category.equals("T-Shirts", true) }
        "Dresses"      -> all.filter { it.category.equals("Dresses", true) }
        else           -> all
    }
}
