package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemVerticalProductListBinding
import com.app.kiranachoice.db.Product

class VerticalProductsAdapter(private val listener : ProductListener) :
    RecyclerView.Adapter<VerticalProductsAdapter.VerticalProductViewHolder>() {

    var data = listOf<Product>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalProductViewHolder {
        return VerticalProductViewHolder.from(parent)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VerticalProductViewHolder, position: Int) {
        holder.bind(data[position], listener)

        holder.binding.btnDecrease.setOnClickListener {
            holder.binding.quantityLayout.visibility = View.GONE
            holder.binding.btnAddToCart.visibility = View.VISIBLE
        }

//        if (list[position].productPackagingSize.size == 1) {
//            holder.binding.productWeight.visibility = View.VISIBLE
//            holder.binding.spinnerPackaging.visibility = View.GONE
//        } else {
//            holder.binding.productWeight.visibility = View.GONE
//            holder.binding.spinnerPackaging.visibility = View.VISIBLE
//        }
//
//        val packagingSize = arrayOfNulls<String>(list[position].productPackagingSize.size)
//        for (i in list[position].productPackagingSize.indices) {
//            packagingSize[i] = list[position].productPackagingSize[i].packagingSize.toString()
//        }

//        val arrayAdapter = ArrayAdapter(
//            holder.itemView.context,
//            R.layout.spinner_item,
//            packagingSize
//        )
//
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        holder.binding.spinnerPackaging.adapter = arrayAdapter

    }

    class VerticalProductViewHolder private constructor(val binding: ItemVerticalProductListBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(product: Product, listener: ProductListener) {
            binding.productListener = listener
            binding.productModel = product
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): VerticalProductViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemVerticalProductListBinding.inflate(layoutInflater, parent, false)
                return VerticalProductViewHolder(binding)
            }
        }
    }

    interface ProductListener {
        fun addItem(product: Product)
    }
}