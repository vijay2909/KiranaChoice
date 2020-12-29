package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.CategoryModel
import com.app.kiranachoice.data.domain.Category
import com.app.kiranachoice.databinding.ItemCategory1LayoutBinding


class Category1Adapter(private val listener: CategoryClickListener) :
    RecyclerView.Adapter<Category1Adapter.CategoryViewHolder>() {

    var list = listOf<Category>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            ItemCategory1LayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        view.categoryInterface = listener
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class CategoryViewHolder(private val binding: ItemCategory1LayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.category = category
            binding.executePendingBindings()
        }
    }

    interface CategoryClickListener {
        fun onCategoryItemClick(category: Category)
    }
}
