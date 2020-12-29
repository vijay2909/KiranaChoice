package com.app.kiranachoice.views.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentCartBinding
import com.app.kiranachoice.data.db.CartDatabase
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.listeners.CartListener
import com.app.kiranachoice.data.CouponModel
import com.app.kiranachoice.recyclerView_adapters.CartItemAdapter
import com.app.kiranachoice.recyclerView_adapters.CouponsAdapter
import com.app.kiranachoice.repositories.DataRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar


class CartFragment : Fragment(), CartListener, CouponsAdapter.CouponApplyListener {

    private var _bindingCart: FragmentCartBinding? = null
    private val binding get() = _bindingCart!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var cartItemAdapter: CartItemAdapter

    private var couponPosition: Int = -1

    private lateinit var viewModel: CartViewModel

    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingCart = FragmentCartBinding.inflate(inflater, container, false)

        // Initialize cart adapter
        cartItemAdapter = CartItemAdapter(this)
        binding.recyclerViewCartList.apply {
            adapter = cartItemAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        // initialize viewModel
        val localDatabase = CartDatabase.getInstance(requireContext().applicationContext)
        val cartViewModelFactory = CartViewModelFactory(DataRepository(localDatabase.databaseDao))
        viewModel = ViewModelProvider(this, cartViewModelFactory).get(CartViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.showCoupon = false

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

        viewModel.allCartItems.observe(viewLifecycleOwner, { list ->
            list?.let { cartItems ->
                binding.isListEmpty = cartItems.isEmpty()
                setupText(cartItems.count())
                cartItemAdapter.submitList(cartItems)
            }
        })


        binding.btnPlaceOrder.setOnClickListener {
//            view.findNavController().navigate(CartFragmentDirections.actionCartFragmentToAddressFragment(viewModel.totalAmount.value.toString(), viewModel.couponCode, viewModel.couponDescription.value))
        }

        val couponsAdapter = CouponsAdapter(this)
        binding.bottomSheet.recyclerViewCouponList.adapter = couponsAdapter

        viewModel.couponsList.observe(viewLifecycleOwner, {
            couponsAdapter.list = it
        })


         viewModel.toastForAlreadyAppliedCoupon.observe(viewLifecycleOwner, {
             if (it) {
                 Toast.makeText(requireContext(), "Already applied! you can use the coupon once in a month.", Toast.LENGTH_LONG).show()
             }
         })


        viewModel.showCoupon.observe(viewLifecycleOwner, {
            if (it) {
                binding.showCoupon = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }else{
                binding.showCoupon = false
            }
        })
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.show()
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
        // show delete confirmation dialog before remove product from cart
        if (!this::dialog.isInitialized) {
            dialog = AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure to remove product from cart?")
                .setPositiveButton(
                    "Yes"
                ) { dialog, _ ->
                    viewModel.removeCartItem(cartItem)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
        }
        dialog.show()
    }


    override fun onQuantityChange(cartItem: CartItem, quantity: Int) {
        viewModel.updateQuantity(cartItem, quantity)
    }


    override fun onCouponApplied(couponModel: CouponModel, position: Int) {
        couponPosition = position
        if (couponModel.isActive) {
            if (couponModel.upToPrice.toString()
                    .toDouble() <= viewModel.totalAmount.value.toString().toDouble()
            ) {
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

}


class CartViewModelFactory(private val dataRepository: DataRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(dataRepository = dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}