package com.app.kiranachoice.viewpager_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.SlidingImageLayoutBinding
import com.app.kiranachoice.models.Banner


class HomeTopBannerAdapter(private val imageList: List<Banner>) :
    RecyclerView.Adapter<HomeTopBannerAdapter.SliderViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        return SliderViewHolder(
            SlidingImageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    class SliderViewHolder(val binding: SlidingImageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Banner) {
            binding.setVariable(BR.banner, data)
            binding.executePendingBindings()
        }
    }
}