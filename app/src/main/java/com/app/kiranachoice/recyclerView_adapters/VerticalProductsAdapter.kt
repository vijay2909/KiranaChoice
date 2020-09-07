package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.ItemVerticalProductListBinding
import com.app.kiranachoice.models.ProductModel

class VerticalProductsAdapter(private val list: List<ProductModel>) :
    RecyclerView.Adapter<VerticalProductsAdapter.VerticalProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalProductViewHolder {
        return VerticalProductViewHolder(
            ItemVerticalProductListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VerticalProductViewHolder, position: Int) {
        holder.bind(list[position])

        holder.binding.btnAddToCart.setOnClickListener {
            holder.binding.quantityLayout.visibility = View.VISIBLE
            holder.binding.btnAddToCart.visibility = View.GONE
        }

        holder.binding.btnDecrease.setOnClickListener {
            holder.binding.quantityLayout.visibility = View.GONE
            holder.binding.btnAddToCart.visibility = View.VISIBLE
        }

        if (list[position].productPackagingSize.size == 1) {
            holder.binding.productWeight.visibility = View.VISIBLE
            holder.binding.spinnerPackaging.visibility = View.GONE
        } else {
            holder.binding.productWeight.visibility = View.GONE
            holder.binding.spinnerPackaging.visibility = View.VISIBLE
        }

        val packagingSize = arrayOfNulls<String>(list[position].productPackagingSize.size)
        for(i in list[position].productPackagingSize.indices){
            packagingSize[i] = list[position].productPackagingSize[i].packagingSize.toString()
        }

        val arrayAdapter = ArrayAdapter(
            holder.itemView.context,
            R.layout.spinner_item,
            packagingSize
        )

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.binding.spinnerPackaging.adapter = arrayAdapter

    }

    companion object {
        private const val TAG = "VerticalProductsAdapter"
    }

    class VerticalProductViewHolder(val binding: ItemVerticalProductListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(productModel: ProductModel) {
            binding.setVariable(BR.productModel, productModel)
            binding.executePendingBindings()
        }
    }
}