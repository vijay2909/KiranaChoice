package com.app.kiranachoice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.ItemCategory1LayoutBinding
import com.app.kiranachoice.models.Category1Model

class Category1Adapter(private val list: List<Category1Model>?) :
    RecyclerView.Adapter<Category1Adapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemCategory1LayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
//        holder.bind(list[position])
    }

    override fun getItemCount(): Int = 9

    class CategoryViewHolder(private val binding: ItemCategory1LayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryModel: Category1Model) {
            binding.setVariable(BR.model, categoryModel)
            binding.executePendingBindings()
        }
    }
}