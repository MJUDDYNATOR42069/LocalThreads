package com.example.ecommerceapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerceapp.databinding.ViewHolderCartBinding
import com.example.ecommerceapp.helpers.ChangeNumberItemsListener
import com.example.ecommerceapp.helpers.ManagementCart
import com.example.ecommerceapp.model.ItemsModel

class CartAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    context: Context,
    var changeNumberItemsListener: ChangeNumberItemsListener? = null
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val managementCart = ManagementCart(context)

    class ViewHolder(val binding: ViewHolderCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewHolderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItemSelected[position]

        holder.binding.apply {
            titleTxt.text = item.title
            feeEachItemTxt.text = "R${item.price}"
            totalEachItemTxt.text = "R${String.format(java.util.Locale.getDefault(), "%.2f", item.numberInCart * item.price)}"
            numberItemTxt.text = item.numberInCart.toString()

            if (item.picUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.picUrl[0])
                    .into(pic)
            } else {
                Glide.with(holder.itemView.context).clear(pic)
                pic.setImageDrawable(null)
            }

            plusCartBtn.setOnClickListener {
                val currentPosition = holder.absoluteAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    managementCart.plusItem(listItemSelected, currentPosition, object : ChangeNumberItemsListener {
                        override fun onChanged() {
                            notifyItemChanged(currentPosition)
                            changeNumberItemsListener?.onChanged()
                        }
                    })
                }
            }

            minusCartBtn.setOnClickListener {
                val currentPosition = holder.absoluteAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    managementCart.minusItem(listItemSelected, currentPosition, object : ChangeNumberItemsListener {
                        override fun onChanged() {
                            notifyItemChanged(currentPosition)
                            changeNumberItemsListener?.onChanged()
                        }
                    })
                }
            }
        }
    }

    override fun getItemCount(): Int = listItemSelected.size
}