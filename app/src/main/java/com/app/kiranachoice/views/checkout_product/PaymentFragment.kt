package com.app.kiranachoice.views.checkout_product

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private var _bindingPayment: FragmentPaymentBinding? = null
    private val binding get() = _bindingPayment!!

    private lateinit var viewModel: CheckoutViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val factory = CheckoutViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory).get(CheckoutViewModel::class.java)
        _bindingPayment = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.paymentRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.isPaymentMethodSelect = true
            when (checkedId) {
                R.id.rbOnline -> {
                    binding.btnPlaceOrder.text = getString(R.string.continue_text)
                }
                R.id.rbCash -> {
                    binding.btnPlaceOrder.text = getString(R.string.placed_order)
                }
            }
        }

        viewModel.allProducts.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) viewModel.cartItemList = it
            setProductListTextWithPrice()
        })

    }


    private fun setProductListTextWithPrice() {
        binding.productListDetailsLayout.removeAllViews()
        viewModel.cartItemList.forEach { product ->
            val relativeLayout = RelativeLayout(requireContext())

            // create text view for product name
            val textProductName = TextView(requireContext())
            textProductName.setTextColor(Color.BLACK)
            textProductName.id = View.generateViewId()
            textProductName.textSize = 15f
            textProductName.text = product.productTitle

            // create text view to show product quantity x product price
            val textSellingPriceAndQuantity = TextView(requireContext())
            textSellingPriceAndQuantity.textSize = 12f
            textSellingPriceAndQuantity.text =
                getString(R.string.quantity).plus(" ${product.quantity}")
                    .plus(" ${getString(R.string.multiply)}")
                    .plus(" ${getString(R.string.rupee)}")
                    .plus(" ${product.productPrice}")

            // create text view for product total price
            val textProductPrice = TextView(requireContext())
            val totalPrice =
                product.quantity.toInt().times(product.productPrice.toInt())
            textProductPrice.id = View.generateViewId()
            textProductPrice.setTextColor(Color.BLACK)
            textProductPrice.textSize = 15f
            textProductPrice.text = getString(R.string.rupee).plus(" $totalPrice")

            val param0 = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            param0.setMargins(16,0,0,24)

            val param1 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            param1.setMargins(0,0,16,0)
            param1.addRule(RelativeLayout.ALIGN_PARENT_START)
            param1.addRule(RelativeLayout.START_OF, textProductPrice.id )

            val param2 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            param2.setMargins(16,0,0,0)
            param2.addRule(RelativeLayout.BELOW, textProductName.id)

            val param3 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            param3.addRule(RelativeLayout.ALIGN_PARENT_END)


            relativeLayout.layoutParams = param0
            textProductName.layoutParams = param1
            textSellingPriceAndQuantity.layoutParams = param2
            textProductPrice.layoutParams = param3


            relativeLayout.addView(textProductName)
            relativeLayout.addView(textSellingPriceAndQuantity)
            relativeLayout.addView(textProductPrice)

            binding.productListDetailsLayout.addView(relativeLayout)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _bindingPayment = null
    }
}