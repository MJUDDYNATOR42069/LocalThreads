package com.example.ecommerceapp.helpers

import android.content.Context
import com.example.ecommerceapp.model.ItemsModel

class ManagementCart(private val context: Context) {

    fun plusItem(list: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {

        list[position].numberInCart += 1
        listener.onChanged()
    }

    fun minusItem(list: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {

        if (list[position].numberInCart > 1) {
            list[position].numberInCart -= 1
        }
        listener.onChanged()
    }
}