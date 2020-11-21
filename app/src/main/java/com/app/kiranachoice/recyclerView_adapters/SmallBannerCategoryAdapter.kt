package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemCategory2LayoutBinding
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.models.Category2Model

class SmallBannerCategoryAdapter : RecyclerView.Adapter<SmallBannerCategoryAdapter.Category2ViewHolder>() {

    var list = listOf<Category1Model>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Category2ViewHolder {
        return Category2ViewHolder(
            ItemCategory2LayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: Category2ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class Category2ViewHolder(private val binding: ItemCategory2LayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Category1Model) {
            binding.model = data
            binding.executePendingBindings()
        }
    }
}