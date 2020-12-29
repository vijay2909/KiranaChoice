package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.domain.Category
import com.app.kiranachoice.databinding.ItemCategory2LayoutBinding

class SmallBannerCategoryAdapter(val listener: Category1Adapter.CategoryClickListener) :
    RecyclerView.Adapter<SmallBannerCategoryAdapter.Category2ViewHolder>() {

    var list = listOf<Category>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Category2ViewHolder {
        val view =
            ItemCategory2LayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view.clickListener = listener
        return Category2ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: Category2ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class Category2ViewHolder(private val binding: ItemCategory2LayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Category) {
            binding.model = data
            binding.executePendingBindings()
        }
    }
}