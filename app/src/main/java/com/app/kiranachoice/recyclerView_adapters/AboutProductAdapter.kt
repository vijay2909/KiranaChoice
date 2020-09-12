package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemAboutProductBinding
import com.app.kiranachoice.models.AboutProductModel


class AboutProductAdapter :
    RecyclerView.Adapter<AboutProductAdapter.AboutProductViewHolder>() {

    var list = listOf<AboutProductModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutProductViewHolder {
        return AboutProductViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AboutProductViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class AboutProductViewHolder(private val binding: ItemAboutProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: AboutProductModel) {
            binding.aboutProductModel = model
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AboutProductViewHolder {
                return AboutProductViewHolder(
                    ItemAboutProductBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }
}