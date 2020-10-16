package com.app.kiranachoice.views.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.kiranachoice.MainViewModel
import com.app.kiranachoice.MainViewModelFactory
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentCartBinding
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.CouponModel
import com.app.kiranachoice.recyclerView_adapters.CartItemAdapter
import com.app.kiranachoice.recyclerView_adapters.CouponsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar

class CartFragment : Fragment(), CartItemAdapter.CartListener, CouponsAdapter.CouponApplyListener {


    private var _bindingCart: FragmentCartBinding? = null
    private val binding get() = _bindingCart!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var viewModel: MainViewModel
    private lateinit var cartItemAdapter: CartItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainViewModelFactory = MainViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), mainViewModelFactory)
            .get(MainViewModel::class.java)

        _bindingCart = FragmentCartBinding.inflate(inflater, container, false)

        binding.mainViewModel = viewModel
        binding.showCoupon = false
        binding.lifecycleOwner = viewLifecycleOwner

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.couponBottomSheet)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCouponApply.setOnClickListener {
            if (binding.btnCouponApply.text == getString(R.string.apply_now))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else {
                viewModel.removeCoupon()
                view.findNavController().navigate(R.id.action_cartFragment_self)
            }
        }

        binding.bottomSheet.btnApply.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.bottomSheet.btnClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        cartItemAdapter = CartItemAdapter(this)
        binding.recyclerViewCartList.adapter = cartItemAdapter

        binding.recyclerViewCartList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewModel.allCartItems.observe(viewLifecycleOwner, {
            it?.let {
                binding.isListEmpty = it.isEmpty()
                setupText(it.count())
                cartItemAdapter.submitList(it)
                viewModel.getTotalPayableAmount(it) // calculate total amount
            }
        })


        binding.btnPlaceOrder.setOnClickListener {
            view.findNavController().navigate(CartFragmentDirections.actionCartFragmentToAddressFragment(viewModel.totalAmount.value.toString(), viewModel.couponCode, viewModel.couponDescription.value))
        }

        val couponsAdapter = CouponsAdapter(this)
        binding.bottomSheet.recyclerViewCouponList.adapter = couponsAdapter

        viewModel.couponList.observe(viewLifecycleOwner, {
            couponsAdapter.list = it
        })

        viewModel.eventDeleteItem.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.eventDeleteItemFinished()
                view.findNavController().navigate(R.id.action_cartFragment_self)
            }
        })

        viewModel.snackBarForAlreadyAppliedCoupon.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.snackBarEventFinished()
                Snackbar.make(
                    view,
                    "Already applied! you can use the coupon once in a month.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })

        viewModel.showCoupon.observe(viewLifecycleOwner, {
            if (it) {
                binding.showCoupon = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                viewModel.showCouponFinished()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.show()
        viewModel.getAllCartItems()
    }

    override fun onPause() {
        super.onPause()
        // update cart items details [[ e.g. quantity of product ]]
        updateCartItemsDetails()
    }


    private fun updateCartItemsDetails() {
        val totalView = binding.recyclerViewCartList.childCount
        for (index in 0 until totalView) {
            val v = binding.recyclerViewCartList.layoutManager?.findViewByPosition(index)
            val productName: TextView? = v?.findViewById(R.id.productName)
            val quantity: TextView? = v?.findViewById(R.id.userQuantity)
            viewModel.updateProduct(productName?.text.toString(), quantity?.text.toString())
        }
    }

    private fun setupText(totalProducts: Int) {
        val itemFound =
            resources.getQuantityString(R.plurals.price_n_items, totalProducts, totalProducts)
        binding.textProductItem.text = itemFound
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingCart = null
    }


    override fun removeCartItem(cartItem: CartItem) {
        viewModel.removeCartItem(cartItem)
    }


    override fun onQuantityChange(
        cartItem: CartItem,
        amountPlus: Int?,
        amountMinus: Int?,
        mrpAndPriceDifference: Int
    ) {
        if (amountPlus != null) viewModel.setTotalAmount(amountPlus, null, mrpAndPriceDifference)
        else viewModel.setTotalAmount(null, amountMinus, mrpAndPriceDifference)
    }

    override fun onCouponApplied(couponModel: CouponModel, position: Int) {
        Log.i(TAG, "onCouponApplied: called")
        if (couponModel.isActive) {
            Log.i(TAG, "coupon is active.")
            if (couponModel.upToPrice.toString().toDouble() <= viewModel.totalAmount.value.toString().toDouble()
            ) {
                Log.i(TAG, "coupon going to applied")
                viewModel.couponApplied(couponModel)
            } else {
                Snackbar.make(
                    requireView(),
                    "Coupon Valid on order value greater than Rs. ${couponModel.upToPrice}.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            Snackbar.make(requireView(), "Coupon Expired.", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "CartFragment"
    }
}