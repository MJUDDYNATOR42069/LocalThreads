package com.example.ecommerceapp

import com.example.ecommerceapp.model.Product

object ProductRepository {

    val recommended: List<Product> = listOf(
        Product("1", "Hoodie (h)", "New", "L", 499.0, "Black", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=600"),
        Product("2", "Dress (d)", "Dresses", "M", 599.0, "Red", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=600"),
        Product("10", "Burgundy Corduroy Jacket", "New", "L", 850.0, "Burgundy", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1548883354-7622d03aca27?w=600"),
        Product("11", "Milano Cream Corduroy Jacket", "New", "M", 799.0, "Cream", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600"),
        Product("16", "Minimalist Off-White Jacket", "New", "XL", 899.0, "Off-White", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1544441893-675973e31985?w=600")
    )

    val trending: List<Product> = listOf(
        Product("3", "Cap (c)", "Accessories", "Free", 199.0, "Blue", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=600"),
        Product("4", "Sneakers (s)", "Shoes", "8", 899.0, "White", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600"),
        Product("12", "Two-Tone Penny Loafers", "Shoes", "9", 950.0, "Black/White", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600"),
        Product("13", "Flame Suede Buckle Clogs", "Shoes", "8", 650.0, "Brown", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1603808033192-082d6919d3e1?w=600"),
        Product("15", "Classic Black Suede Clogs", "Shoes", "7", 599.0, "Black", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=600")
    )

    val popular: List<Product> = listOf(
        Product("5", "Jacket (j)", "New", "XL", 799.0, "Brown", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600"),
        Product("6", "Jeans (je)", "Second-hand", "32", 450.0, "Blue", "LocalThreads", "Second-hand",
            "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600"),
        Product("7", "Light Blue Wide-Leg Jeans", "Jeans", "32", 450.0, "Light Blue", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600"),
        Product("8", "Dark Wash Slim Fit Jeans", "Jeans", "30", 499.0, "Dark Blue", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1542272604-780c36856842?w=600"),
        Product("9", "Vintage Street Baggy Jeans", "Second-hand", "34", 520.0, "Medium Blue", "LocalThreads", "Second-hand",
            "https://images.unsplash.com/photo-1604176354204-9268737828e4?w=600"),
        Product("14", "Striped Quarter-Zip Polo", "T-Shirts", "L", 399.0, "Black/White", "LocalThreads", "New",
            "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=600")
    )

    val all: List<Product> = recommended + trending + popular

    fun byCategory(category: String): List<Product> = when (category) {
        ProductListActivity.CATEGORY_RECOMMENDED -> recommended
        ProductListActivity.CATEGORY_TRENDING    -> trending
        ProductListActivity.CATEGORY_POPULAR     -> popular
        ProductListActivity.CATEGORY_ALL         -> all
        else                                     -> all
    }

    /** Filters the full catalog by a chip label (New / Second-hand / T-Shirts / Dresses / Shoes / Jeans). */
    fun byChip(chip: String): List<Product> = when (chip) {
        "New"          -> all.filter { it.condition.equals("New", true) }
        "Second-hand"  -> all.filter { it.condition.equals("Second-hand", true) }
        "T-Shirts"     -> all.filter { it.category.equals("T-Shirts", true) }
        "Dresses"      -> all.filter { it.category.equals("Dresses", true) }
        "Shoes"        -> all.filter { it.category.equals("Shoes", true) }
        "Jeans"        -> all.filter { it.category.equals("Jeans", true) }
        else           -> all
    }
}
