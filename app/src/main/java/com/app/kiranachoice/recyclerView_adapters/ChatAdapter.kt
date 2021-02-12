package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.RowAdminMessageItemBinding
import com.app.kiranachoice.databinding.RowUserMessageItemBinding
import com.app.kiranachoice.data.network_models.Chat
import com.app.kiranachoice.utils.USER

class ChatAdapter : ListAdapter<Chat, RecyclerView.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.time == newItem.time
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == 0) {
            UserMessageViewHolder(
                RowUserMessageItemBinding.inflate(inflater, parent, false)
            )
        } else {
            AdminMessageViewHolder(
                RowAdminMessageItemBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserMessageViewHolder -> {
                holder.bind(getItem(position))
            }
            is AdminMessageViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.sender == USER) 0
        else 1
    }


    class UserMessageViewHolder(val binding: RowUserMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.executePendingBindings()
        }
    }

    class AdminMessageViewHolder(val binding: RowAdminMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.executePendingBindings()
        }
    }


}