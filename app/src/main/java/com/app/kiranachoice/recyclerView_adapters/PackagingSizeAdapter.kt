package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemPackagingSizeCardBinding
import com.app.kiranachoice.data.PackagingSizeModel

private var lastCheckedPosition = 0

class PackagingSizeAdapter : RecyclerView.Adapter<PackagingSizeAdapter.PackagingSizeViewHolder>() {

    var list = listOf<PackagingSizeModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackagingSizeViewHolder {
        return PackagingSizeViewHolder(
            ItemPackagingSizeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PackagingSizeViewHolder, position: Int) {
        holder.bind(list[position])
        holder.binding.sizeCard.isChecked = position == lastCheckedPosition

    }

    override fun getItemCount(): Int = list.size

    inner class PackagingSizeViewHolder(val binding: ItemPackagingSizeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: PackagingSizeModel) {
            binding.packaging = model
            binding.executePendingBindings()
        }

        init {
            binding.sizeCard.setOnClickListener {
                val copyOfLastCheckedPosition = lastCheckedPosition
                lastCheckedPosition = adapterPosition
                notifyItemChanged(copyOfLastCheckedPosition)
                notifyItemChanged(lastCheckedPosition)
            }
        }
    }
}