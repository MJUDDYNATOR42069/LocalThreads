package com.example.ecommerceapp.model

import java.io.Serializable

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val size: String = "",
    val price: Double = 0.0,
    val colour: String = "",
    val brand: String = "",
    val condition: String = "",
    val imageUrl: String = ""
) : Serializable
