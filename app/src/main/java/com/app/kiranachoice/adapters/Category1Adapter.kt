package com.app.kiranachoice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.ItemCategory1LayoutBinding
import com.app.kiranachoice.models.Category1Model


class Category1Adapter(
    private val list: List<Category1Model>,
    private val listener: CategoryClickListener
) :
    RecyclerView.Adapter<Category1Adapter.CategoryViewHolder>() {

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
        fun bind(categoryModel: Category1Model) {
            binding.setVariable(BR.categoryModel, categoryModel)
            binding.executePendingBindings()
        }
    }

    interface CategoryClickListener {
        fun onCategoryItemClick(categoryModel: Category1Model)
    }
}
