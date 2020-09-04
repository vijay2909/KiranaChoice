package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.ItemSubCategoryBinding
import com.app.kiranachoice.models.SubCategoryModel

class SubCategoryAdapter(
    private val list: List<SubCategoryModel>,
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
        fun bind(subCategoryModel: SubCategoryModel) {
            binding.setVariable(BR.subCategoryModel, subCategoryModel)
        }
    }

    interface SubCategoryClickListener {
        fun onSubCategoryItemClicked(subCategoryModel: SubCategoryModel)
    }
}