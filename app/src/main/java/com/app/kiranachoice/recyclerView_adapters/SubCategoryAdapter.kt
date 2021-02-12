package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.domain.SubCategory
import com.app.kiranachoice.databinding.ItemSubCategoryBinding

class SubCategoryAdapter(
    private val list: List<SubCategory>,
    private val listener: SubCategoryClickListener
) :
    RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val view =
            ItemSubCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view.subCategoryInterface = listener
        return SubCategoryViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class SubCategoryViewHolder(val binding: ItemSubCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subCategory: SubCategory) {
            binding.subCategory = subCategory
            binding.executePendingBindings()
        }
    }

    interface SubCategoryClickListener {
        fun onSubCategoryItemClicked(subCategory: SubCategory)
    }
}