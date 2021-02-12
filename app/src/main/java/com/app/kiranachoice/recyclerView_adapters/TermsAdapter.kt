package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemTermBinding
import com.app.kiranachoice.data.network_models.Terms

class TermsAdapter : RecyclerView.Adapter<TermsAdapter.TermsViewHolder>() {

    var list = listOf<Terms>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermsViewHolder {
        return TermsViewHolder(
            ItemTermBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TermsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class TermsViewHolder(val binding: ItemTermBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(terms: Terms) {
            binding.terms = terms
            binding.executePendingBindings()
        }
    }

}