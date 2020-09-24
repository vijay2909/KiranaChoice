package com.app.kiranachoice.viewpager_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.SlidingImageLayoutBinding
import com.app.kiranachoice.models.BannerImageModel


class HomeTopBannerAdapter : RecyclerView.Adapter<HomeTopBannerAdapter.SliderViewHolder>() {

    var list = listOf<BannerImageModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        return SliderViewHolder(
            SlidingImageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class SliderViewHolder(val binding: SlidingImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bannerImageModel: BannerImageModel) {
            binding.bannerImageModel = bannerImageModel
            binding.executePendingBindings()
        }
    }
}