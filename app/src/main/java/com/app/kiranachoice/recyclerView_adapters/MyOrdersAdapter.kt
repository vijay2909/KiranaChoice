package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemMyOrderItemBinding
import com.app.kiranachoice.models.BookItemOrderModel

class MyOrdersAdapter(private val listener : OrderClickListener) : RecyclerView.Adapter<MyOrdersAdapter.OrdersViewHolder>() {

    var list = listOf<BookItemOrderModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val view = ItemMyOrderItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        view.listener = listener
        return OrdersViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class OrdersViewHolder(val binding: ItemMyOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bookItemOrderModel: BookItemOrderModel) {
            binding.model = bookItemOrderModel
            binding.executePendingBindings()
        }
    }

    interface OrderClickListener {
        fun onOrderClicked(bookItemOrderModel: BookItemOrderModel)
    }
}