package com.example.ecommerceapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerceapp.databinding.ViewHolderCartBinding
import com.example.ecommerceapp.model.ItemsModel
import com.example.ecommerceapp.helpers.ManagementCart
import com.example.ecommerceapp.helpers.ChangeNumberItemsListener
import kotlin.math.roundToInt

class CartAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    context: Context,
    var changeNumberItemsListener: ChangeNumberItemsListener? = null
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val managementCart = ManagementCart(context)

    class ViewHolder(var binding: ViewHolderCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewHolderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItemSelected[position]

        holder.binding.apply {
            titleTxt.text = item.title
            feeEachItemTxt.text = "R${item.price}"
            totalEachItemTxt.text = "R${String.format("%.2f", item.numberInCart * item.price)}"
            numberItemTxt.text = item.numberInCart.toString()

            if (item.picUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.picUrl[0])
                    .into(pic)
            }

            plusCartBtn.setOnClickListener {
                managementCart.plusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyItemChanged(position)
                        changeNumberItemsListener?.onChanged()
                    }
                })
            }

            minusCartBtn.setOnClickListener {
                managementCart.minusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyItemChanged(position)
                        changeNumberItemsListener?.onChanged()
                    }
                })
            }
        }
    }

    override fun getItemCount(): Int = listItemSelected.size
}