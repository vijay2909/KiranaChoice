package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemOrderDetailBinding
import com.app.kiranachoice.models.Product
import com.app.kiranachoice.utils.CANCELED

class ProductDetailAdapter(val listener : CancelOrderListener) : RecyclerView.Adapter<ProductDetailAdapter.ProductViewHolder>() {

    var list = listOf<Product>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(list[position])

        if (list[position].status == CANCELED){
            holder.binding.canceled.visibility = View.VISIBLE
        }

        holder.binding.btnCancelOrder.setOnClickListener {
            listener.onOrderCancel(position)
        }
    }


    override fun getItemCount(): Int = list.size

    class ProductViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.product = product
            binding.executePendingBindings()
        }
    }

    interface CancelOrderListener {
        fun onOrderCancel(position: Int)
    }
}