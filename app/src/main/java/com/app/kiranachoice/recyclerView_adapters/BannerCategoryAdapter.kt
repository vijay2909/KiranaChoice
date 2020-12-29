package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemBannerCategoryLayoutBinding
import com.app.kiranachoice.data.BannerImageModel

class BannerCategoryAdapter(private val list: List<BannerImageModel>?) :
    RecyclerView.Adapter<BannerCategoryAdapter.BannerCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerCategoryViewHolder {
        return BannerCategoryViewHolder(
            ItemBannerCategoryLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = 4

    override fun onBindViewHolder(holder: BannerCategoryViewHolder, position: Int) {
//        holder.bind(list[position])
    }

    class BannerCategoryViewHolder(private val binding: ItemBannerCategoryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: BannerImageModel) {

        }
    }
}