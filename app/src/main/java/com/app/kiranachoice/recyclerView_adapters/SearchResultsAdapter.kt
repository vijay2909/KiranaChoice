package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.ItemSearchBinding
import com.app.kiranachoice.db.Product

class SearchResultsAdapter(private val list: List<Product>) :
    RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(
            ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class SearchResultViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(value: Product) {
            binding.setVariable(BR.productModel, value)
            binding.executePendingBindings()
        }
    }
}