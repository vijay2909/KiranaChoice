package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemSearchBinding
import com.app.kiranachoice.data.SearchWord

class SearchResultsAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<SearchWord, SearchResultsAdapter.SearchResultViewHolder>(DiffCallbacks()) {

    class DiffCallbacks : DiffUtil.ItemCallback<SearchWord>() {
        override fun areItemsTheSame(oldItem: SearchWord, newItem: SearchWord): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: SearchWord, newItem: SearchWord): Boolean {
            return oldItem.productName == newItem.productName
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(
            ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val searchWord = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(searchWord)
        }
        holder.bind(searchWord)
    }

    class SearchResultViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(value: SearchWord) {
            binding.searchWord = value
            binding.executePendingBindings()
        }
    }

    class OnClickListener(val clickListener: (searchWord: SearchWord) -> Unit) {
        fun onClick(searchWord: SearchWord) = clickListener(searchWord)
    }
}