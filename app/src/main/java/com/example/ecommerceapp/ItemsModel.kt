package com.example.ecommerceapp.model

import java.io.Serializable

data class ItemsModel(
    var title: String = "",
    var price: Double = 0.0,
    var numberInCart: Int = 1,
    var picUrl: ArrayList<String> = ArrayList()
) : Serializable