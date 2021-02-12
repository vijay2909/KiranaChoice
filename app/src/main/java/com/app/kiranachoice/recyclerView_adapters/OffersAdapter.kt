package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.RecyclerViewOfferCardBinding
import com.app.kiranachoice.data.network_models.OfferModel

class OffersAdapter(private val listener: OfferClickListener) :
    RecyclerView.Adapter<OffersAdapter.OffersViewHolder>() {

    var list = listOf<OfferModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OffersViewHolder {
        val view = RecyclerViewOfferCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view.clickListener = listener
        return OffersViewHolder(view)
    }

    override fun onBindViewHolder(holder: OffersViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class OffersViewHolder(val binding: RecyclerViewOfferCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(offersModel: OfferModel) {
            binding.model = offersModel
            binding.executePendingBindings()
        }
    }

    interface OfferClickListener {
        fun onOfferItemClicked(offersModel: OfferModel)
    }
}