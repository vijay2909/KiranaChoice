package com.app.kiranachoice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.ItemSubCategoryBinding
import com.app.kiranachoice.models.SubCategoryModel

class SubCategoryAdapter(private val list: List<SubCategoryModel>) :
    RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        return SubCategoryViewHolder(
            ItemSubCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class SubCategoryViewHolder(val binding: ItemSubCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subCategoryModel: SubCategoryModel) {
            binding.setVariable(BR.subCategoryModel, subCategoryModel)
        }
    }

    interface OnSubCategoryClickListener {
        fun onSubCategoryItemClicked(category: String?, subCategoryName: String?)
    }
}