package com.app.kiranachoice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.ItemCategory1LayoutBinding
import com.app.kiranachoice.models.CategoryModel

class CategoryAdapter(
    private val list: List<CategoryModel>,
    private val listener: CategoryClickListener
) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = ItemCategory1LayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view.categoryInterface = listener
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class CategoryViewHolder(private val binding: ItemCategory1LayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryModel: CategoryModel) {
            binding.setVariable(BR.categoryModel, categoryModel)
            binding.executePendingBindings()
        }
    }

    interface CategoryClickListener {
        fun onCategoryItemClick(categoryModel: CategoryModel)
    }
}