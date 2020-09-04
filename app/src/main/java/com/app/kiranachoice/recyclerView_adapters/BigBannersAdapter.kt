package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemBannerLayoutBinding
import com.app.kiranachoice.models.BannerImageModel

class BigBannersAdapter(private val list: List<BannerImageModel>?) :
    RecyclerView.Adapter<BigBannersAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(
            ItemBannerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = 3

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
//        holder.bind(list[position])
    }

    class BannerViewHolder(private val binding: ItemBannerLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: BannerImageModel) {

        }
    }
}