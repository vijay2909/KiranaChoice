package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemAddressLayoutBinding
import com.app.kiranachoice.models.AddressModel

private var lastCheckedPosition = 0

class AddressAdapter(private val listener : AddressCardClickListener): RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    var list = listOf<AddressModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            ItemAddressLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(list[position], listener)

        holder.binding.menuButton.setOnClickListener {
            holder.binding.customizeCard.visibility = View.VISIBLE
        }

        holder.binding.addressCard.isChecked = position == lastCheckedPosition

    }

    override fun getItemCount(): Int = list.size

    inner
    class AddressViewHolder(val binding: ItemAddressLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(addressModel: AddressModel, listener: AddressCardClickListener) {
            binding.addressCardClickListener = listener
            binding.addressModel = addressModel
            binding.executePendingBindings()
        }

        init {
            binding.addressCard.setOnClickListener {
                if (binding.customizeCard.visibility == View.VISIBLE){
                    binding.customizeCard.visibility = View.GONE
                }
                val copyOfLastCheckedPosition = lastCheckedPosition
                lastCheckedPosition = adapterPosition
                notifyItemChanged(copyOfLastCheckedPosition)
                notifyItemChanged(lastCheckedPosition)
            }
        }
    }

    interface AddressCardClickListener{
        fun onEditButtonClicked(addressModel: AddressModel)
        fun onDeleteButtonClicked(addressModel: AddressModel)
    }
}