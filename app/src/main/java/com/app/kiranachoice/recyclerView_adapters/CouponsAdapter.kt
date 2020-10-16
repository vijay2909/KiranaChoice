package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.RawCouponItemBinding
import com.app.kiranachoice.models.CouponModel
import com.google.android.material.textview.MaterialTextView

private var currentPosition = -1

class CouponsAdapter(private val listener: CouponApplyListener) :
    RecyclerView.Adapter<CouponsAdapter.CouponsViewHolder>() {

    var list = listOf<CouponModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponsViewHolder {
        val view = RawCouponItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CouponsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponsViewHolder, position: Int) {
        holder.bind(list[position])

        holder.binding.couponTermsLayout.removeAllViews()

        list[position].termsList?.forEach {
            val termTextView = MaterialTextView(holder.itemView.context)
            termTextView.text = holder.itemView.context.getString(R.string.dash).plus(" $it")
            holder.binding.couponTermsLayout.addView(termTextView)
        }

        //if the position is equals to the item position which is to be expanded
        if (currentPosition == position) {
            //creating an animation
            val slideDown: Animation = AnimationUtils.loadAnimation(
                holder.itemView.context,
                R.anim.slide_down
            )

            //toggling visibility
            holder.binding.couponTermsLayout.visibility = View.VISIBLE
            holder.binding.textViewDetails.visibility = View.GONE

            //adding sliding effect
            holder.binding.couponTermsLayout.startAnimation(slideDown)
        }

        holder.binding.textViewDetails.setOnClickListener {
            //getting the position of the item to expand it
            currentPosition = position
            //reloding the list
            notifyDataSetChanged()
        }

        holder.binding.applyCouponButton.setOnClickListener {
            holder.binding.applyCouponButton.isEnabled = false
            listener.onCouponApplied(list[position], position)
        }
    }

    override fun getItemCount(): Int = list.size

    class CouponsViewHolder(val binding: RawCouponItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(couponModel: CouponModel) {
            binding.couponModel = couponModel
            binding.executePendingBindings()
        }
    }

    interface CouponApplyListener {
        fun onCouponApplied(couponModel: CouponModel, position: Int)
    }
}