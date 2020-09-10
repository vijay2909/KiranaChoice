package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemVerticalProductListBinding
import com.app.kiranachoice.models.ProductModel
import com.google.android.material.snackbar.Snackbar

class VerticalProductsAdapter(private val listener: ProductListener) :
    RecyclerView.Adapter<VerticalProductsAdapter.VerticalProductViewHolder>() {

    var data = listOf<ProductModel>()
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


        fun bind(productModel: ProductModel, listener: ProductListener) {
            binding.productListener = listener
            binding.productModel = productModel
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): VerticalProductViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemVerticalProductListBinding.inflate(layoutInflater, parent, false)
                return VerticalProductViewHolder(binding)
            }
        }

        init {
            binding.btnIncrease.setOnClickListener {
                var quantity = Integer.parseInt(binding.userQuantity.text.toString())
                if (quantity < 5) ++quantity else Snackbar.make(
                    binding.root,
                    "You can't get maximum 5 quantity.",
                    Snackbar.LENGTH_SHORT
                ).show()
                binding.userQuantity.text = quantity.toString()
            }
            binding.btnDecrease.setOnClickListener {
                var quantity = Integer.parseInt(binding.userQuantity.text.toString())
                if (quantity > 0) --quantity
                binding.userQuantity.text = quantity.toString()
            }
        }
    }

    interface ProductListener {
        fun addItemToCart(productModel: ProductModel)
    }
}