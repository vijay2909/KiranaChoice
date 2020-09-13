package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.databinding.ItemSearchBinding
import com.app.kiranachoice.models.ProductModel

class SearchResultsAdapter :
    ListAdapter<ProductModel, SearchResultsAdapter.SearchResultViewHolder>(DiffCallbacks()) {

    class DiffCallbacks : DiffUtil.ItemCallback<ProductModel>() {
        override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem.product_key == newItem.product_key
        }

        override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(
            ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun submitList(list: List<ProductModel>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SearchResultViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(value: ProductModel) {
            binding.setVariable(BR.productModel, value)
            binding.executePendingBindings()
        }
    }
}